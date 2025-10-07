using AutoMapper;
using Microservice.MapsPointsUser.Api.IService;
using Microservice.MapsPointsUser.Api.Model.Dto;
using Microservice.MapsPointsUser.Api.Service.IService;
using NetTopologySuite.Geometries;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;

namespace Microservice.MapsPointsUser.Api.Service
{
    public class PoiService : IPoiService
    {
        private readonly IPoiRepository _repo;
        private readonly IMapper _mapper;
        private readonly GeometryFactory _geometryFactory;

        public PoiService(IPoiRepository repo, IMapper mapper, GeometryFactory geometryFactory)
        {
            _repo = repo;
            _mapper = mapper;
            _geometryFactory = geometryFactory;
        }

        public async Task<PoiDto> CreateAsync(PoiDto createDto, CancellationToken ct = default)
        {
            var point = _geometryFactory.CreatePoint(new Coordinate(createDto.Longitude, createDto.Latitude));
            point.SRID = 4326;

            var poi = new Model.Poi
            {
                PoiId = Guid.NewGuid(),
                BusinessId = createDto.BusinessId,
                Name = createDto.Name,
                Category = createDto.Category,
                Description = createDto.Description,
                Location = point,
                IsActive = createDto.IsActive,
                CreatedAt = DateTime.UtcNow
            };

            await _repo.AddAsync(poi, ct);
            await _repo.SaveChangesAsync(ct);
            return _mapper.Map<PoiDto>(poi);
        }

        public async Task<bool> DeleteAsync(Guid id, CancellationToken ct = default)
        {
            var poi = await _repo.GetByIdAsync(id, ct);
            if (poi == null) return false;
            await _repo.DeleteAsync(poi, ct);
            await _repo.SaveChangesAsync(ct);
            return true;
        }

        public async Task<PoiDto?> GetByIdAsync(Guid id, CancellationToken ct = default)
        {
            var poi = await _repo.GetByIdAsync(id, ct);
            return poi == null ? null : _mapper.Map<PoiDto>(poi);
        }

        public async Task<List<PoiDto>> GetNearbyAsync(double lat, double lng, double radiusMeters, string? category = null, string? name = null, CancellationToken ct = default)
        {
            var origin = _geometryFactory.CreatePoint(new Coordinate(lng, lat));
            origin.SRID = 4326;

            var pois = await _repo.GetNearbyAsync(origin, radiusMeters, category, name, ct);
            return _mapper.Map<List<PoiDto>>(pois);
        }

        public async Task<bool> UpdateAsync(Guid id, PoiDto updateDto, CancellationToken ct = default)
        {
            var poi = await _repo.GetByIdAsync(id, ct);
            if (poi == null) return false;

            poi.Name = updateDto.Name;
            poi.Category = updateDto.Category;
            poi.Description = updateDto.Description;
            poi.BusinessId = updateDto.BusinessId;
            poi.IsActive = updateDto.IsActive;

            var pt = _geometryFactory.CreatePoint(new Coordinate(updateDto.Longitude, updateDto.Latitude));
            pt.SRID = 4326;
            poi.Location = pt;

            await _repo.UpdateAsync(poi, ct);
            await _repo.SaveChangesAsync(ct);
            return true;
        }
    }
}
