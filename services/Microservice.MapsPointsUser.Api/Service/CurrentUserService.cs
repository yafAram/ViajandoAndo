using Microservice.MapsPointsUser.Api.IService;
using Microsoft.AspNetCore.Http;
using System.Security.Claims;

namespace Microservice.MapsPointsUser.Api.Service
{
    public class CurrentUserService : ICurrentUserService
    {
        private readonly IHttpContextAccessor _httpContextAccessor;

        public CurrentUserService(IHttpContextAccessor httpContextAccessor)
        {
            _httpContextAccessor = httpContextAccessor;
        }

        public Guid? GetUserId()
        {
            var user = _httpContextAccessor.HttpContext?.User;
            var sub = user?.FindFirst(ClaimTypes.NameIdentifier)?.Value
                      ?? user?.FindFirst("sub")?.Value;
            return Guid.TryParse(sub, out var guid) ? guid : null;
        }

        public string? GetUserRole()
        {
            var user = _httpContextAccessor.HttpContext?.User;
            return user?.FindFirst(ClaimTypes.Role)?.Value
                   ?? user?.FindFirst("role")?.Value;
        }

        public IEnumerable<string>? GetRoles()
        {
            var user = _httpContextAccessor.HttpContext?.User;
            return user?.Claims
                        .Where(c => c.Type == ClaimTypes.Role || c.Type == "role")
                        .Select(c => c.Value);
        }
    }
}
