using AutoMapper;
using Microservice.MapsPointsUser.Api.IService;
using Microservice.MapsPointsUser.Api.Model;
using Microservice.MapsPointsUser.Api.Model.Dto;
using Microservice.MapsPointsUser.Api.Service.IService;
using NetTopologySuite.Geometries;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;
using Route = Microservice.MapsPointsUser.Api.Model.Route;

namespace Microservice.MapsPointsUser.Api.Service
{
    public class RouteService : IRouteService
    {
        private readonly IRouteRepository _routeRepo;
        private readonly IPoiRepository _poiRepo;
        private readonly IRoutingProvider _routingProvider;
        private readonly ICurrentUserService _currentUser;
        private readonly IMapper _mapper;
        private readonly GeometryFactory _geometryFactory;

        public RouteService(
            IRouteRepository routeRepo,
            IPoiRepository poiRepo,
            IRoutingProvider routingProvider,
            ICurrentUserService currentUser,
            IMapper mapper,
            GeometryFactory geometryFactory)
        {
            _routeRepo = routeRepo;
            _poiRepo = poiRepo;
            _routingProvider = routingProvider;
            _currentUser = currentUser;
            _mapper = mapper;
            _geometryFactory = geometryFactory;
        }

        public async Task<RouteDto> SaveRouteAsync(SaveRouteRequestDto request, CancellationToken ct = default)
        {
            // GetUserId() devuelve Guid? — si es null lanzamos excepción (no autenticado)
            var uid = _currentUser.GetUserId() ?? throw new UnauthorizedAccessException();

            var routeEntity = new Route
            {
                RouteId = Guid.NewGuid(),
                UserId = uid, // uid es Guid aquí (no .Value)
                Name = request.Name,
                CreatedAt = DateTime.UtcNow,
                TotalDistanceMeters = request.TotalDistanceMeters,
                TotalDurationSeconds = request.TotalDurationSeconds,
                Polyline = request.Polyline,
                Mode = request.Mode,
                Waypoints = request.Waypoints.Select(w => {
                    var pt = _geometryFactory.CreatePoint(new Coordinate(w.Longitude, w.Latitude));
                    pt.SRID = 4326;
                    return new Waypoint
                    {
                        WaypointId = w.WaypointId == Guid.Empty ? Guid.NewGuid() : w.WaypointId,
                        PoiId = w.PoiId,
                        Order = w.Order,
                        ArrivalETA = w.ArrivalETA,
                        Location = pt
                    };
                }).ToList()
            };

            await _routeRepo.AddAsync(routeEntity, ct);
            await _routeRepo.SaveChangesAsync(ct);

            return _mapper.Map<RouteDto>(routeEntity);
        }

        public async Task<bool> DeleteRouteAsync(Guid routeId, CancellationToken ct = default)
        {
            var route = await _routeRepo.GetByIdAsync(routeId, ct);
            if (route == null) return false;

            var uid = _currentUser.GetUserId() ?? throw new UnauthorizedAccessException();
            if (route.UserId != uid) throw new UnauthorizedAccessException();

            await _routeRepo.DeleteAsync(route, ct);
            await _routeRepo.SaveChangesAsync(ct);
            return true;
        }

        public async Task<RouteDto?> GetRouteByIdAsync(Guid routeId, CancellationToken ct = default)
        {
            var route = await _routeRepo.GetByIdAsync(routeId, ct);
            return route == null ? null : _mapper.Map<RouteDto>(route);
        }

        public async Task<List<RouteDto>> GetRoutesByUserAsync(Guid userId, CancellationToken ct = default)
        {
            var list = await _routeRepo.GetByUserIdAsync(userId, ct);
            return _mapper.Map<List<RouteDto>>(list);
        }

        public async Task<RouteOptimizeResponseDto> OptimizeRouteAsync(RouteOptimizeRequestDto request, CancellationToken ct = default)
        {
            var coords = await ResolveCoordinatesAsync(request, ct);
            return await _routingProvider.OptimizeAsync(coords, request.Mode, ct);
        }

        public async Task<RouteOptimizeResponseDto?> RecalculateRouteAsync(Guid routeId, RouteOptimizeRequestDto request, CancellationToken ct = default)
        {
            var route = await _routeRepo.GetByIdAsync(routeId, ct);
            if (route == null) return null;

            var uid = _currentUser.GetUserId() ?? throw new UnauthorizedAccessException();
            if (route.UserId != uid) throw new UnauthorizedAccessException();

            var coords = await ResolveCoordinatesAsync(request, ct);
            return await _routingProvider.OptimizeAsync(coords, request.Mode, ct);
        }

        private async Task<List<Coordinate>> ResolveCoordinatesAsync(RouteOptimizeRequestDto req, CancellationToken ct)
        {
            if (req == null) throw new ArgumentNullException(nameof(req));
            if (req.Origin == null) throw new ArgumentException("Origin es requerido");
            if (req.Waypoints == null || req.Waypoints.Count == 0) throw new ArgumentException("Waypoints es requerido");

            const int MAX_POINTS = 50;
            if (req.Waypoints.Count + 1 > MAX_POINTS)
                throw new ArgumentException($"El número máximo permitido de puntos es {MAX_POINTS - 1}");

            var list = new List<Coordinate>();

            // origin
            if (double.IsNaN(req.Origin.Lng) || double.IsNaN(req.Origin.Lat))
                throw new ArgumentException("Origin coordenadas inválidas");
            list.Add(new Coordinate(req.Origin.Lng, req.Origin.Lat));

            // resolver poiIds en batch
            var poiIds = req.Waypoints.Where(w => w.PoiId.HasValue && w.Coordinate == null)
                                      .Select(w => w.PoiId!.Value).Distinct().ToList();

            var poiMap = new Dictionary<Guid, Poi?>();
            foreach (var pid in poiIds)
            {
                var p = await _poiRepo.GetByIdAsync(pid, ct);
                poiMap[pid] = p;
            }

            foreach (var w in req.Waypoints)
            {
                // Preferir coordinate explícita del cliente
                if (w.Coordinate != null)
                {
                    if (double.IsNaN(w.Coordinate.Lng) || double.IsNaN(w.Coordinate.Lat) ||
                        w.Coordinate.Lat < -90 || w.Coordinate.Lat > 90 || w.Coordinate.Lng < -180 || w.Coordinate.Lng > 180)
                        throw new ArgumentException("Waypoint coordinate fuera de rango");
                    list.Add(new Coordinate(w.Coordinate.Lng, w.Coordinate.Lat));
                    continue;
                }

                if (w.PoiId.HasValue)
                {
                    if (!poiMap.TryGetValue(w.PoiId.Value, out var poi) || poi == null)
                        throw new ArgumentException($"POI {w.PoiId} no encontrado");
                    if (poi.Location == null) throw new ArgumentException($"POI {w.PoiId} no tiene Location");
                    list.Add(new Coordinate(poi.Location.X, poi.Location.Y));
                    continue;
                }

                throw new ArgumentException("Cada waypoint debe tener PoiId o Coordinate");
            }

            return list;
        }

    }
}
