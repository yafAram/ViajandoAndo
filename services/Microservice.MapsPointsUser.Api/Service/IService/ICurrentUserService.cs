namespace Microservice.MapsPointsUser.Api.IService
{
    /// <summary>
    /// Servicio que abstrae la lectura del usuario actual (claims del JWT).
    /// </summary>
    public interface ICurrentUserService
    {
        Guid? GetUserId();
        string? GetUserRole();              // uno
        IEnumerable<string>? GetRoles();    // varios
    }
}
