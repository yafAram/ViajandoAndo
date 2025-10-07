using AutoMapper;
using Microservice.MapsPointsUser.Api.Model;
using Microservice.MapsPointsUser.Api.Model.Dto;
using NetTopologySuite;
using NetTopologySuite.Geometries;

namespace Microservice.MapsPointsUser.Api.Mapping
{
    public class MapsProfile : Profile
    {
        public MapsProfile()
        {
            // Entity -> DTO
            CreateMap<Poi, PoiDto>()
                .ForMember(dest => dest.Latitude, opt => opt.MapFrom(src => src.Location != null ? src.Location.Y : 0.0))
                .ForMember(dest => dest.Longitude, opt => opt.MapFrom(src => src.Location != null ? src.Location.X : 0.0));

            // DTO -> Entity: expression-only (no body) para evitar errores de EF/AutoMapper
            CreateMap<PoiDto, Poi>()
                .ForMember(dest => dest.Location,
                    opt => opt.MapFrom(src =>
                        NtsGeometryServices.Instance
                            .CreateGeometryFactory(4326)
                            .CreatePoint(new Coordinate(src.Longitude, src.Latitude))));

            // Waypoint mapeos
            CreateMap<Waypoint, WaypointDto>()
                .ForMember(d => d.Latitude, o => o.MapFrom(s => s.Location != null ? s.Location.Y : 0.0))
                .ForMember(d => d.Longitude, o => o.MapFrom(s => s.Location != null ? s.Location.X : 0.0));

            CreateMap<WaypointDto, Waypoint>()
                .ForMember(dest => dest.Location,
                    opt => opt.MapFrom(src =>
                        NtsGeometryServices.Instance
                            .CreateGeometryFactory(4326)
                            .CreatePoint(new Coordinate(src.Longitude, src.Latitude))));

            // Route <-> RouteDto (AutoMapper mapea listas de waypoints automáticamente)
            CreateMap<Microservice.MapsPointsUser.Api.Model.Route, RouteDto>().ReverseMap();
        }
    }
}
