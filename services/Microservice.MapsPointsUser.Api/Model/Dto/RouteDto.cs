namespace Microservice.MapsPointsUser.Api.Model.Dto
{
    public class RouteDto
    {
        public Guid RouteId { get; set; }
        public Guid UserId { get; set; }
        public string Name { get; set; }
        public DateTime CreatedAt { get; set; }
        public double TotalDistanceMeters { get; set; }
        public int TotalDurationSeconds { get; set; }
        public string Polyline { get; set; }
        public string Mode { get; set; }
        public List<WaypointDto> Waypoints { get; set; }
    }
}
