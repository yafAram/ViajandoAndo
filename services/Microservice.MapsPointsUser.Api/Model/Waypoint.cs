using NetTopologySuite.Geometries;

namespace Microservice.MapsPointsUser.Api.Model
{
    public class Waypoint
    {
        public Guid WaypointId { get; set; }
        public Guid RouteId { get; set; }
        public Guid? PoiId { get; set; }
        public Point Location { get; set; }
        public int Order { get; set; }
        public DateTime? ArrivalETA { get; set; }

        public Route Route { get; set; }
        public Poi Poi { get; set; }
    }
}
