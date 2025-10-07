using NetTopologySuite.Geometries;

namespace Microservice.MapsPointsUser.Api.Model
{
    public class Poi
    {
        public Guid PoiId { get; set; }
        public Guid? BusinessId { get; set; }
        public string Name { get; set; }
        public string Category { get; set; }
        public string Description { get; set; }
        public Point Location { get; set; }
        public bool IsActive { get; set; }
        public DateTime CreatedAt { get; set; }
    }
}
