namespace Microservice.MapsPointsUser.Api.Model
{
    public class Route
    {
        public Guid RouteId { get; set; }
        public Guid UserId { get; set; }
        public string Name { get; set; }
        public DateTime CreatedAt { get; set; }
        public double TotalDistanceMeters { get; set; }
        public int TotalDurationSeconds { get; set; }
        public string Polyline { get; set; }
        public string Mode { get; set; }

        // Propiedad de navegación
        public List<Waypoint> Waypoints { get; set; }
    }
}
