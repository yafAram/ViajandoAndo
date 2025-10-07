namespace Microservice.MapsPointsUser.Api.Model.Dto
{
    public class RouteOptimizeResponseDto
    {
        public string Polyline { get; set; } = string.Empty;
        public double TotalDistanceMeters { get; set; }
        public int TotalDurationSeconds { get; set; }
        public List<RouteStepDto> Steps { get; set; } = new();
    }

    public class RouteStepDto
    {
        public double DistanceMeters { get; set; }
        public int DurationSeconds { get; set; }
        public string Instruction { get; set; } = string.Empty;
        public double Lat { get; set; }
        public double Lng { get; set; }
    }
}
