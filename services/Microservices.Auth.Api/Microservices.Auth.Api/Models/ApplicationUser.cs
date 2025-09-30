using Microsoft.AspNetCore.Identity;

namespace Microservices.Auth.Api.Models
{
    public class ApplicationUser : IdentityUser
    {
        public string Name { get; set; }
    }
}
