namespace Microservice.MapsPointsUser.Api.Model.Dto
{
    public class WaypointDto
    {
        public Guid WaypointId { get; set; }
        public Guid RouteId { get; set; }
        public Guid? PoiId { get; set; }
        public double Latitude { get; set; }
        public double Longitude { get; set; }
        public int Order { get; set; }
        public DateTime? ArrivalETA { get; set; }
    }
}
