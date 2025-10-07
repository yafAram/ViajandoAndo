using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microservice.MapsPointsUser.Api.IService;
using Microservice.MapsPointsUser.Api.Model.Dto;

namespace Microservice.MapsPointsUser.Api.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    [Produces("application/json")]
    [Authorize]
    public class RoutesController : ControllerBase
    {
        private readonly IRouteService _routeService;
        private readonly ICurrentUserService _currentUser;
        private readonly ILogger<RoutesController> _logger;

        public RoutesController(IRouteService routeService, ICurrentUserService currentUser, ILogger<RoutesController> logger)
        {
            _routeService = routeService;
            _currentUser = currentUser;
            _logger = logger;
        }

        /// <summary>
        /// Calcular/optimizar ruta (no persiste). Body: RouteOptimizeRequestDto
        /// </summary>
        [HttpPost("optimize")]
        [ProducesResponseType(typeof(RouteOptimizeResponseDto), 200)]
        [ProducesResponseType(400)]
        public async Task<IActionResult> Optimize([FromBody] RouteOptimizeRequestDto req)
        {
            if (req == null) return BadRequest("Request body is required");
            var ct = HttpContext.RequestAborted;

            try
            {
                var res = await _routeService.OptimizeRouteAsync(req, ct);
                return Ok(res);
            }
            catch (ArgumentException aex)
            {
                return BadRequest(aex.Message);
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error Optimize route");
                return StatusCode(500, "Error interno optimizando ruta");
            }
        }

        /// <summary>
        /// Recalcula una ruta existente (verifica que el usuario propietario sea el mismo)
        /// </summary>
        [HttpPost("recalculate/{routeId:guid}")]
        [ProducesResponseType(typeof(RouteOptimizeResponseDto), 200)]
        [ProducesResponseType(404)]
        [ProducesResponseType(403)]
        public async Task<IActionResult> Recalculate([FromRoute] Guid routeId, [FromBody] RouteOptimizeRequestDto req)
        {
            if (req == null) return BadRequest("Request body is required");
            var ct = HttpContext.RequestAborted;

            try
            {
                var res = await _routeService.RecalculateRouteAsync(routeId, req, ct);
                if (res == null) return NotFound();
                return Ok(res);
            }
            catch (UnauthorizedAccessException)
            {
                return Forbid();
            }
            catch (ArgumentException aex)
            {
                return BadRequest(aex.Message);
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error Recalculate route");
                return StatusCode(500, "Error interno recalculando ruta");
            }
        }

        /// <summary>
        /// Guardar ruta (historial) - el servicio validará el userId desde el token
        /// </summary>
        [HttpPost]
        [ProducesResponseType(typeof(RouteDto), 201)]
        [ProducesResponseType(400)]
        public async Task<IActionResult> Save([FromBody] SaveRouteRequestDto req)
        {
            if (req == null) return BadRequest("Request body is required");
            var ct = HttpContext.RequestAborted;

            try
            {
                var saved = await _routeService.SaveRouteAsync(req, ct);
                return CreatedAtAction(nameof(GetById), new { id = saved.RouteId }, saved);
            }
            catch (UnauthorizedAccessException)
            {
                return Forbid();
            }
            catch (ArgumentException aex)
            {
                return BadRequest(aex.Message);
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error Save route");
                return StatusCode(500, "Error interno guardando ruta");
            }
        }

        /// <summary>
        /// Listar historial de rutas del usuario autenticado
        /// </summary>
        [HttpGet]
        [ProducesResponseType(typeof(IEnumerable<RouteDto>), 200)]
        public async Task<IActionResult> GetMyRoutes()
        {
            var ct = HttpContext.RequestAborted;
            var userId = _currentUser.GetUserId();
            if (userId == null) return Forbid();

            var list = await _routeService.GetRoutesByUserAsync(userId.Value, ct);
            return Ok(list);
        }

        /// <summary>
        /// Obtener ruta por id (solo propietario o admin)
        /// </summary>
        [HttpGet("{id:guid}")]
        [ProducesResponseType(typeof(RouteDto), 200)]
        [ProducesResponseType(404)]
        [ProducesResponseType(403)]
        public async Task<IActionResult> GetById([FromRoute] Guid id)
        {
            var ct = HttpContext.RequestAborted;
            var route = await _routeService.GetRouteByIdAsync(id, ct);
            if (route == null) return NotFound();

            var uid = _currentUser.GetUserId();
            var roles = _currentUser.GetRoles(); // si tu ICurrentUserService expone roles

            // permitir si es admin o propietario
            if (uid == null) return Forbid();
            if (route.UserId != uid.Value && !(roles?.Contains("Admin") ?? false))
                return Forbid();

            return Ok(route);
        }

        /// <summary>
        /// Eliminar ruta (solo propietario o admin)
        /// </summary>
        [HttpDelete("{id:guid}")]
        [ProducesResponseType(204)]
        [ProducesResponseType(404)]
        [ProducesResponseType(403)]
        public async Task<IActionResult> Delete([FromRoute] Guid id)
        {
            var ct = HttpContext.RequestAborted;
            try
            {
                var ok = await _routeService.DeleteRouteAsync(id, ct);
                if (!ok) return NotFound();
                return NoContent();
            }
            catch (UnauthorizedAccessException)
            {
                return Forbid();
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error deleting route");
                return StatusCode(500, "Error interno eliminando ruta");
            }
        }
    }
}
