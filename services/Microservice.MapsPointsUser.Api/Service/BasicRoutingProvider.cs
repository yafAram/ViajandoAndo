using Microservice.MapsPointsUser.Api.Service.IService;
using Microservice.MapsPointsUser.Api.Model.Dto;
using NetTopologySuite.Geometries;
using System.Text;

namespace Microservice.MapsPointsUser.Api.Service
{
    /// <summary>
    /// Implementación local para MVP: calcula distancias por Haversine y genera pasos simples.
    /// Reemplazable por provider que llame a OpenRouteService / OSRM.
    /// </summary>
    public class BasicRoutingProvider : IRoutingProvider
    {
        private const double SpeedDriving = 13.9; // m/s ~50km/h
        private const double SpeedWalking = 1.4;  // m/s ~5km/h

        public Task<RouteOptimizeResponseDto> OptimizeAsync(List<Coordinate> orderedCoords, string mode = "driving", CancellationToken ct = default)
        {
            if (orderedCoords == null || orderedCoords.Count < 2)
            {
                return Task.FromResult(new RouteOptimizeResponseDto
                {
                    Polyline = "[]",
                    TotalDistanceMeters = 0,
                    TotalDurationSeconds = 0,
                    Steps = new List<RouteStepDto>()
                });
            }

            var steps = new List<RouteStepDto>();
            double totalDistance = 0;
            double totalDuration = 0;
            var speed = (mode?.ToLower() == "walking") ? SpeedWalking : SpeedDriving;

            for (int i = 0; i < orderedCoords.Count - 1; i++)
            {
                var a = orderedCoords[i];
                var b = orderedCoords[i + 1];
                var dist = HaversineDistanceMeters(a.Y, a.X, b.Y, b.X);
                var duration = (int)Math.Round(dist / speed);

                steps.Add(new RouteStepDto
                {
                    DistanceMeters = dist,
                    DurationSeconds = duration,
                    Instruction = $"Avanzar al siguiente punto ({i + 1})",
                    Lat = b.Y,
                    Lng = b.X
                });

                totalDistance += dist;
                totalDuration += duration;
            }

            var polySb = new StringBuilder();
            polySb.Append("[");
            polySb.Append(string.Join(",", orderedCoords.Select(c => $"[{c.Y.ToString(System.Globalization.CultureInfo.InvariantCulture)},{c.X.ToString(System.Globalization.CultureInfo.InvariantCulture)}]")));
            polySb.Append("]");

            var res = new RouteOptimizeResponseDto
            {
                Polyline = polySb.ToString(),
                TotalDistanceMeters = totalDistance,
                TotalDurationSeconds = (int)Math.Round(totalDuration),
                Steps = steps
            };

            return Task.FromResult(res);
        }

        private static double HaversineDistanceMeters(double lat1, double lon1, double lat2, double lon2)
        {
            const double R = 6371000; // m
            var dLat = ToRad(lat2 - lat1);
            var dLon = ToRad(lon2 - lon1);
            var a = Math.Sin(dLat / 2) * Math.Sin(dLat / 2) +
                    Math.Cos(ToRad(lat1)) * Math.Cos(ToRad(lat2)) *
                    Math.Sin(dLon / 2) * Math.Sin(dLon / 2);
            var c = 2 * Math.Atan2(Math.Sqrt(a), Math.Sqrt(1 - a));
            return R * c;
        }

        private static double ToRad(double deg) => deg * (Math.PI / 180.0);
    }
}
