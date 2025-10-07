namespace Microservice.MapsPointsUser.Api.Model.Dto
{
    public class SaveRouteRequestDto
    {
        public string Name { get; set; } = string.Empty;
        public string Polyline { get; set; } = string.Empty;
        public double TotalDistanceMeters { get; set; }
        public int TotalDurationSeconds { get; set; }
        public string Mode { get; set; } = "driving";
        public List<WaypointDto> Waypoints { get; set; } = new();
    }
}
