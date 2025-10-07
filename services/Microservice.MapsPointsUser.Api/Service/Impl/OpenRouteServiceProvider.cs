// File: Service/Impl/OpenRouteServiceProvider.cs
using Microservice.MapsPointsUser.Api.Model.Dto;
using Microservice.MapsPointsUser.Api.Service.IService;
using NetTopologySuite.Geometries;
using System.Linq;
using System.Net.Http;
using System.Net.Http.Headers;
using System.Text;
using System.Text.Json;
using System.Threading;
using System.Threading.Tasks;

namespace Microservice.MapsPointsUser.Api.Service.Impl
{
    public class OpenRouteServiceProvider : IRoutingProvider
    {
        private readonly IHttpClientFactory _httpFactory;
        private readonly IConfiguration _config;
        private readonly ILogger<OpenRouteServiceProvider> _logger;

        public OpenRouteServiceProvider(IHttpClientFactory httpFactory, IConfiguration config, ILogger<OpenRouteServiceProvider> logger)
        {
            _httpFactory = httpFactory;
            _config = config;
            _logger = logger;
        }

        public async Task<RouteOptimizeResponseDto> OptimizeAsync(List<Coordinate> orderedCoords, string mode = "driving", CancellationToken ct = default)
        {
            if (orderedCoords == null || orderedCoords.Count < 2)
                throw new ArgumentException("Se requieren al menos dos coordenadas (origen y destino).");

            var apiKey = _config["OpenRouteService:ApiKey"];
            var baseUrl = _config["OpenRouteService:Url"] ?? "https://api.openrouteservice.org";

            if (string.IsNullOrWhiteSpace(apiKey))
                throw new InvalidOperationException("OpenRouteService:ApiKey no configurado en appsettings.json");

            var profile = mode?.ToLower() switch
            {
                "walking" or "foot" => "foot-walking",
                "bicycle" or "cycling" => "cycling-regular",
                _ => "driving-car"
            };

            var client = _httpFactory.CreateClient("ors");
            if (client.BaseAddress == null) client.BaseAddress = new Uri(baseUrl);

            // Construir body: pedir geometry en formato geojson para facilitar parseo
            var coordsArray = orderedCoords.Select(c => new[] { c.X, c.Y }).ToArray();
            var bodyObj = new
            {
                coordinates = coordsArray,
                instructions = true,
                geometry = true,
                units = "m",
                elevation = false
            };

            var jsonOptions = new JsonSerializerOptions { PropertyNamingPolicy = JsonNamingPolicy.CamelCase };
            var bodyJson = JsonSerializer.Serialize(bodyObj, jsonOptions);

            var url = $"v2/directions/{profile}/geojson";
            using var request = new HttpRequestMessage(HttpMethod.Post, url);
            // ORS uses the API key in Authorization header (just the key)
            request.Headers.Accept.Clear();
            request.Headers.Accept.Add(new MediaTypeWithQualityHeaderValue("application/geo+json"));
            request.Headers.Accept.Add(new MediaTypeWithQualityHeaderValue("application/json"));
            request.Headers.Accept.Add(new MediaTypeWithQualityHeaderValue("*/*"));
            request.Content = new StringContent(bodyJson, Encoding.UTF8, "application/json");

            // Log request (sensible to quitar en producción o filtrar claves)
            _logger.LogDebug("ORS request to {Url} body: {Body}", url, bodyJson);

            using var resp = await client.SendAsync(request, HttpCompletionOption.ResponseHeadersRead, ct);
            var content = await resp.Content.ReadAsStringAsync(ct);

            // Log completo (útil para depuración)
            _logger.LogInformation("ORS response (status {Status}): {Body}", resp.StatusCode, content);

            if (!resp.IsSuccessStatusCode)
            {
                _logger.LogWarning("ORS returned {Status} - {Body}", resp.StatusCode, content);
                throw new HttpRequestException($"ORS response: {resp.StatusCode} - {content}");
            }

            var result = new RouteOptimizeResponseDto();

            try
            {
                using var doc = JsonDocument.Parse(content);
                var root = doc.RootElement;

                // 1) GEOJSON style -> features[]
                if (root.TryGetProperty("features", out var features) &&
                    features.ValueKind == JsonValueKind.Array &&
                    features.GetArrayLength() > 0)
                {
                    var first = features[0];

                    // properties.summary
                    if (first.TryGetProperty("properties", out var props) && props.ValueKind == JsonValueKind.Object)
                    {
                        if (props.TryGetProperty("summary", out var summary) && summary.ValueKind == JsonValueKind.Object)
                        {
                            if (summary.TryGetProperty("distance", out var d) && d.ValueKind != JsonValueKind.Null) result.TotalDistanceMeters = d.GetDouble();
                            if (summary.TryGetProperty("duration", out var du) && du.ValueKind != JsonValueKind.Null) result.TotalDurationSeconds = (int)Math.Round(du.GetDouble());
                        }

                        // segments -> steps
                        if (props.TryGetProperty("segments", out var segments) && segments.ValueKind == JsonValueKind.Array && segments.GetArrayLength() > 0)
                        {
                            var seg0 = segments[0];
                            if (seg0.ValueKind == JsonValueKind.Object && seg0.TryGetProperty("steps", out var stepsArr) && stepsArr.ValueKind == JsonValueKind.Array)
                            {
                                foreach (var s in stepsArr.EnumerateArray())
                                {
                                    var step = new RouteStepDto();
                                    if (s.TryGetProperty("distance", out var sd) && sd.ValueKind != JsonValueKind.Null) step.DistanceMeters = sd.GetDouble();
                                    if (s.TryGetProperty("duration", out var sdu) && sdu.ValueKind != JsonValueKind.Null) step.DurationSeconds = (int)Math.Round(sdu.GetDouble());
                                    if (s.TryGetProperty("instruction", out var instr) && instr.ValueKind == JsonValueKind.String) step.Instruction = instr.GetString() ?? string.Empty;

                                    // localizar coordinate para el step via geometry.coordinates + way_points
                                    if (s.TryGetProperty("way_points", out var wp) && wp.ValueKind == JsonValueKind.Array && wp.GetArrayLength() >= 1)
                                    {
                                        var idx = wp[0].GetInt32();
                                        if (first.TryGetProperty("geometry", out var geometry) && geometry.ValueKind == JsonValueKind.Object)
                                        {
                                            if (geometry.TryGetProperty("coordinates", out var coordsEl))
                                            {
                                                // coordsEl puede ser Array o String
                                                if (coordsEl.ValueKind == JsonValueKind.Array)
                                                {
                                                    // Aplanar posible MultiLineString
                                                    var flat = new List<JsonElement>();
                                                    if (coordsEl.GetArrayLength() > 0)
                                                    {
                                                        var f0 = coordsEl[0];
                                                        if (f0.ValueKind == JsonValueKind.Number || (f0.ValueKind == JsonValueKind.Array && f0.GetArrayLength() > 0 && f0[0].ValueKind == JsonValueKind.Number))
                                                        {
                                                            foreach (var ce in coordsEl.EnumerateArray()) flat.Add(ce);
                                                        }
                                                        else
                                                        {
                                                            foreach (var sub in coordsEl.EnumerateArray())
                                                                if (sub.ValueKind == JsonValueKind.Array)
                                                                    foreach (var ce in sub.EnumerateArray())
                                                                        flat.Add(ce);
                                                        }
                                                    }

                                                    if (idx >= 0 && idx < flat.Count)
                                                    {
                                                        var coordEl = flat[idx];
                                                        if (coordEl.ValueKind == JsonValueKind.Array && coordEl.GetArrayLength() >= 2)
                                                        {
                                                            step.Lng = coordEl[0].GetDouble();
                                                            step.Lat = coordEl[1].GetDouble();
                                                        }
                                                    }
                                                }
                                                else if (coordsEl.ValueKind == JsonValueKind.String)
                                                {
                                                    // geometry codificada como string -> la guardamos (cliente puede decodificar)
                                                    if (string.IsNullOrEmpty(result.Polyline))
                                                        result.Polyline = coordsEl.GetString() ?? string.Empty;
                                                }
                                            }
                                        }
                                    }

                                    result.Steps.Add(step);
                                }
                            }
                        }
                    }

                    // geometry -> coordinates -> construir polyline simple
                    if (first.TryGetProperty("geometry", out var geometryEl) && geometryEl.ValueKind == JsonValueKind.Object)
                    {
                        if (geometryEl.TryGetProperty("coordinates", out var coordsEl))
                        {
                            if (coordsEl.ValueKind == JsonValueKind.String)
                            {
                                // encoded - intentamos decodificar (o dejar para cliente)
                                var enc = coordsEl.GetString();
                                // intentamos decodificar en servidor (precisión 5 o 6)
                                if (!string.IsNullOrEmpty(enc))
                                {
                                    var pts = TryDecodePolyline(enc);
                                    if (pts != null && pts.Count > 0)
                                    {
                                        var sb = new StringBuilder();
                                        foreach (var p in pts) sb.Append($"{p.lat},{p.lng};");
                                        result.Polyline = sb.ToString().TrimEnd(';');
                                    }
                                    else
                                    {
                                        result.Polyline = enc; // fallback
                                    }
                                }
                            }
                            else if (coordsEl.ValueKind == JsonValueKind.Array)
                            {
                                var listCoords = new List<(double lat, double lng)>();
                                if (coordsEl.GetArrayLength() > 0)
                                {
                                    var firstElem = coordsEl[0];
                                    if (firstElem.ValueKind == JsonValueKind.Number || (firstElem.ValueKind == JsonValueKind.Array && firstElem.GetArrayLength() > 0 && firstElem[0].ValueKind == JsonValueKind.Number))
                                    {
                                        foreach (var coord in coordsEl.EnumerateArray())
                                        {
                                            if (coord.ValueKind == JsonValueKind.Array && coord.GetArrayLength() >= 2)
                                            {
                                                var lng = coord[0].GetDouble();
                                                var lat = coord[1].GetDouble();
                                                listCoords.Add((lat, lng));
                                            }
                                        }
                                    }
                                    else
                                    {
                                        foreach (var sub in coordsEl.EnumerateArray())
                                            if (sub.ValueKind == JsonValueKind.Array)
                                                foreach (var coord in sub.EnumerateArray())
                                                    if (coord.ValueKind == JsonValueKind.Array && coord.GetArrayLength() >= 2)
                                                    {
                                                        var lng = coord[0].GetDouble();
                                                        var lat = coord[1].GetDouble();
                                                        listCoords.Add((lat, lng));
                                                    }
                                    }
                                }

                                var sb = new StringBuilder();
                                foreach (var c in listCoords) sb.Append($"{c.lat},{c.lng};");
                                result.Polyline = sb.ToString().TrimEnd(';');
                            }
                        }
                    }
                }
                // 2) 'routes' style
                else if (root.TryGetProperty("routes", out var routes) && routes.ValueKind == JsonValueKind.Array && routes.GetArrayLength() > 0)
                {
                    var r0 = routes[0];
                    if (r0.TryGetProperty("distance", out var dist) && dist.ValueKind != JsonValueKind.Null) result.TotalDistanceMeters = dist.GetDouble();
                    if (r0.TryGetProperty("duration", out var dur) && dur.ValueKind != JsonValueKind.Null) result.TotalDurationSeconds = (int)Math.Round(dur.GetDouble());

                    if (r0.TryGetProperty("legs", out var legs) && legs.ValueKind == JsonValueKind.Array)
                    {
                        foreach (var leg in legs.EnumerateArray())
                        {
                            if (leg.TryGetProperty("steps", out var stepsArr) && stepsArr.ValueKind == JsonValueKind.Array)
                            {
                                foreach (var s in stepsArr.EnumerateArray())
                                {
                                    var step = new RouteStepDto();
                                    if (s.TryGetProperty("distance", out var sd) && sd.ValueKind != JsonValueKind.Null) step.DistanceMeters = sd.GetDouble();
                                    if (s.TryGetProperty("duration", out var sdu) && sdu.ValueKind != JsonValueKind.Null) step.DurationSeconds = (int)Math.Round(sdu.GetDouble());
                                    if (s.TryGetProperty("name", out var instr) && instr.ValueKind == JsonValueKind.String) step.Instruction = instr.GetString() ?? string.Empty;

                                    if (s.TryGetProperty("maneuver", out var mv) && mv.ValueKind == JsonValueKind.Object && mv.TryGetProperty("location", out var loc) && loc.ValueKind == JsonValueKind.Array && loc.GetArrayLength() >= 2)
                                    {
                                        step.Lng = loc[0].GetDouble();
                                        step.Lat = loc[1].GetDouble();
                                    }

                                    result.Steps.Add(step);
                                }
                            }
                        }
                    }

                    if (r0.TryGetProperty("geometry", out var geometryEl) && geometryEl.ValueKind == JsonValueKind.Object && geometryEl.TryGetProperty("coordinates", out var coordsEl2))
                    {
                        if (coordsEl2.ValueKind == JsonValueKind.String)
                        {
                            var enc = coordsEl2.GetString();
                            if (!string.IsNullOrEmpty(enc))
                            {
                                var pts = TryDecodePolyline(enc);
                                if (pts != null && pts.Count > 0)
                                {
                                    var sb = new StringBuilder();
                                    foreach (var p in pts) sb.Append($"{p.lat},{p.lng};");
                                    result.Polyline = sb.ToString().TrimEnd(';');
                                }
                                else result.Polyline = enc;
                            }
                        }
                        else if (coordsEl2.ValueKind == JsonValueKind.Array)
                        {
                            var sb = new StringBuilder();
                            foreach (var coord in coordsEl2.EnumerateArray())
                            {
                                if (coord.ValueKind == JsonValueKind.Array && coord.GetArrayLength() >= 2)
                                {
                                    var lng = coord[0].GetDouble();
                                    var lat = coord[1].GetDouble();
                                    sb.Append($"{lat},{lng};");
                                }
                            }
                            result.Polyline = sb.ToString().TrimEnd(';');
                        }
                    }
                }
                else
                {
                    _logger.LogWarning("ORS response con estructura inesperada. Revisa el contenido: {Body}", content);
                }
            }
            catch (Exception ex)
            {
                _logger.LogWarning(ex, "Error parseando respuesta ORS");
            }

            return result;
        }

        // Intenta decodificar encoded polyline (Google/ORS). Prueba precisión 5 y 6
        private List<(double lat, double lng)>? TryDecodePolyline(string encoded)
        {
            try
            {
                var pts5 = DecodePolyline(encoded, 5);
                if (pts5 != null && pts5.Count > 0) return pts5;

                var pts6 = DecodePolyline(encoded, 6);
                if (pts6 != null && pts6.Count > 0) return pts6;
            }
            catch
            {
                // ignore
            }
            return null;
        }

        // Decodificador genérico polyline (precision param: 5 or 6)
        private List<(double lat, double lng)> DecodePolyline(string encoded, int precision = 5)
        {
            if (string.IsNullOrEmpty(encoded)) return new List<(double lat, double lng)>();

            var index = 0;
            int lat = 0;
            int lng = 0;
            var coordinates = new List<(double lat, double lng)>();
            int shift, result, b;

            while (index < encoded.Length)
            {
                // lat
                shift = 0;
                result = 0;
                do
                {
                    b = encoded[index++] - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20 && index < encoded.Length);
                var dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lat += dlat;

                // lng
                shift = 0;
                result = 0;
                do
                {
                    b = encoded[index++] - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20 && index < encoded.Length);
                var dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lng += dlng;

                var factor = Math.Pow(10, precision);
                coordinates.Add((lat / factor, lng / factor));
            }
            return coordinates;
        }
    }
}
