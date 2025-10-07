using Microservice.MapsPointsUser.Api.Model.Dto;

namespace Microservice.MapsPointsUser.Api.IService
{
    /// <summary>
    /// Lógica de negocio relacionada a Rutas y navegación.
    /// </summary>
    public interface IRouteService
    {
        Task<RouteOptimizeResponseDto> OptimizeRouteAsync(RouteOptimizeRequestDto request, CancellationToken ct = default);
        Task<RouteOptimizeResponseDto?> RecalculateRouteAsync(Guid routeId, RouteOptimizeRequestDto request, CancellationToken ct = default);
        Task<RouteDto> SaveRouteAsync(SaveRouteRequestDto request, CancellationToken ct = default);
        Task<List<RouteDto>> GetRoutesByUserAsync(Guid userId, CancellationToken ct = default);
        Task<RouteDto?> GetRouteByIdAsync(Guid routeId, CancellationToken ct = default);
        Task<bool> DeleteRouteAsync(Guid routeId, CancellationToken ct = default);
    }
}
