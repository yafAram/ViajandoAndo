using Microservice.MapsPointsUser.Api.Model.Dto;
using NetTopologySuite.Geometries;

namespace Microservice.MapsPointsUser.Api.Service.IService
{
    /// <summary>
    /// Abstracción para proveedores de ruteo (ORS, OSRM, o proveedor local de fallback).
    /// </summary>
    public interface IRoutingProvider
    {
        /// <summary>
        /// Recibe coordenadas ordenadas (origin + waypoints) y devuelve polyline, distancia, tiempo y pasos.
        /// </summary>
        Task<RouteOptimizeResponseDto> OptimizeAsync(List<Coordinate> orderedCoords, string mode = "driving", CancellationToken ct = default);
    }
}
