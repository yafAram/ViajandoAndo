using Microservice.MapsPointsUser.Api.Model.Dto;

namespace Microservice.MapsPointsUser.Api.IService
{
    /// <summary>
    /// Lógica de negocio relacionada a POIs (consulta/filtrado).
    /// </summary>
    public interface IPoiService
    {
        Task<List<PoiDto>> GetNearbyAsync(double lat, double lng, double radiusMeters, string? category = null, string? name = null, CancellationToken ct = default);
        Task<PoiDto?> GetByIdAsync(Guid id, CancellationToken ct = default);
        // Métodos opcionales para administración local del microservicio:
        Task<PoiDto> CreateAsync(PoiDto createDto, CancellationToken ct = default);
        Task<bool> UpdateAsync(Guid id, PoiDto updateDto, CancellationToken ct = default);
        Task<bool> DeleteAsync(Guid id, CancellationToken ct = default);
    }
}
