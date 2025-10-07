namespace Microservice.MapsPointsUser.Api.Model.Dto
{
    public class PoiDto
    {
        public Guid PoiId { get; set; }
        public Guid? BusinessId { get; set; }
        public string Name { get; set; }
        public string Category { get; set; }
        public string Description { get; set; }
        public double Latitude { get; set; }
        public double Longitude { get; set; }
        public bool IsActive { get; set; }
        public DateTime CreatedAt { get; set; }
    }
}
