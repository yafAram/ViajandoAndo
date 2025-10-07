namespace Microservice.MapsPointsUser.Api.Model.Dto
{
    public class RouteOptimizeRequestDto
    {
        public CoordinateDto Origin { get; set; } = new();
        /// <summary>
        /// Lista de puntos: cada elemento puede venir como PoiId (referencia a la BD local) o como coordenada.
        /// Si viene PoiId, el servicio debe resolver la coordenada desde la BD local antes de llamar al provider.
        /// </summary>
        public List<CoordinateOrPoiDto> Waypoints { get; set; } = new();
        public string Mode { get; set; } = "driving";
        // Nuevo: comportamiento de recalculo
        public string RecalcMode { get; set; } = "reoptimize_remaining";
    }

    public class CoordinateDto
    {
        public double Lat { get; set; }
        public double Lng { get; set; }
    }

    public class CoordinateOrPoiDto
    {
        public Guid? PoiId { get; set; }
        public CoordinateDto? Coordinate { get; set; }
    }
}
