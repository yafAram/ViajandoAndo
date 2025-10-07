using Route = Microservice.MapsPointsUser.Api.Model.Route;

namespace Microservice.MapsPointsUser.Api.IService
{
    /// <summary>
    /// Repositorio para persistencia de rutas y waypoints.
    /// </summary>
    public interface IRouteRepository
    {
        Task<Route?> GetByIdAsync(Guid id, CancellationToken ct = default);
        Task<List<Route>> GetByUserIdAsync(Guid userId, CancellationToken ct = default);
        Task AddAsync(Route route, CancellationToken ct = default);
        Task DeleteAsync(Route route, CancellationToken ct = default);
        Task UpdateAsync(Route route, CancellationToken ct = default);
        Task SaveChangesAsync(CancellationToken ct = default);
    }
}
