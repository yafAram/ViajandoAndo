using Microservices.Auth.Api.Models;

namespace Microservices.Auth.Api.Service.IService
{
    public interface IJwtTokenGenerator
    {
        /// <summary>
        /// Este metodo se encarga de generar el token con los datos del usuario
        /// </summary>
        /// <param name="applicationUser">el usuario logeado</param>
        /// <returns>una cadena que es el token jwt</returns>
        string GenerateToken(ApplicationUser applicationUser, IEnumerable<string> roles);
    }
}
