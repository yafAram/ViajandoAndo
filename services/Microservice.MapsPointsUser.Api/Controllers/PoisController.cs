using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microservice.MapsPointsUser.Api.IService;
using Microservice.MapsPointsUser.Api.Model.Dto;

namespace Microservice.MapsPointsUser.Api.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    [Produces("application/json")]
    [Authorize] // requiere JWT para la mayoría de rutas (puedes ajustar a [AllowAnonymous] si quieres)
    public class PoisController : ControllerBase
    {
        private readonly IPoiService _poiService;
        private readonly ILogger<PoisController> _logger;

        public PoisController(IPoiService poiService, ILogger<PoisController> logger)
        {
            _poiService = poiService;
            _logger = logger;
        }

        /// <summary>
        /// Obtener POIs cercanos. Requiere lat/lng/radius.
        /// Query params: lat, lng, radius (metros), category (opcional), name (opcional)
        /// </summary>
        [HttpGet]
        [ProducesResponseType(typeof(IEnumerable<PoiDto>), 200)]
        [ProducesResponseType(400)]
        public async Task<IActionResult> GetNearby(
            [FromQuery] double lat,
            [FromQuery] double lng,
            [FromQuery] double radius,
            [FromQuery] string? category = null,
            [FromQuery] string? name = null)
        {
            if (radius <= 0) return BadRequest("radius must be > 0");
            var ct = HttpContext.RequestAborted;

            try
            {
                var list = await _poiService.GetNearbyAsync(lat, lng, radius, category, name, ct);
                return Ok(list);
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error GetNearby");
                return StatusCode(500, "Error interno al obtener POIs");
            }
        }

        /// <summary>
        /// Obtener POI por id
        /// </summary>
        [HttpGet("{id:guid}")]
        [ProducesResponseType(typeof(PoiDto), 200)]
        [ProducesResponseType(404)]
        public async Task<IActionResult> GetById([FromRoute] Guid id)
        {
            var ct = HttpContext.RequestAborted;
            var poi = await _poiService.GetByIdAsync(id, ct);
            if (poi == null) return NotFound();
            return Ok(poi);
        }

        /// <summary>
        /// Crear POI (solo Owner | Admin)
        /// </summary>
        [HttpPost]
        [Authorize(Roles = "Owner,Admin")]
        [ProducesResponseType(typeof(PoiDto), 201)]
        [ProducesResponseType(400)]
        public async Task<IActionResult> Create([FromBody] PoiDto createDto)
        {
            if (!ModelState.IsValid) return BadRequest(ModelState);
            var ct = HttpContext.RequestAborted;

            try
            {
                var created = await _poiService.CreateAsync(createDto, ct);
                return CreatedAtAction(nameof(GetById), new { id = created.PoiId }, created);
            }
            catch (ArgumentException aex)
            {
                return BadRequest(aex.Message);
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error Create POI");
                return StatusCode(500, "Error interno creando POI");
            }
        }

        /// <summary>
        /// Actualizar POI (solo Owner | Admin)
        /// </summary>
        [HttpPut("{id:guid}")]
        [Authorize(Roles = "Owner,Admin")]
        [ProducesResponseType(204)]
        [ProducesResponseType(404)]
        [ProducesResponseType(400)]
        public async Task<IActionResult> Update([FromRoute] Guid id, [FromBody] PoiDto updateDto)
        {
            if (!ModelState.IsValid) return BadRequest(ModelState);
            var ct = HttpContext.RequestAborted;

            try
            {
                var ok = await _poiService.UpdateAsync(id, updateDto, ct);
                if (!ok) return NotFound();
                return NoContent();
            }
            catch (ArgumentException aex)
            {
                return BadRequest(aex.Message);
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error Update POI");
                return StatusCode(500, "Error interno actualizando POI");
            }
        }

        /// <summary>
        /// Eliminar POI (solo Owner | Admin)
        /// </summary>
        [HttpDelete("{id:guid}")]
        [Authorize(Roles = "Owner,Admin")]
        [ProducesResponseType(204)]
        [ProducesResponseType(404)]
        public async Task<IActionResult> Delete([FromRoute] Guid id)
        {
            var ct = HttpContext.RequestAborted;
            try
            {
                var ok = await _poiService.DeleteAsync(id, ct);
                if (!ok) return NotFound();
                return NoContent();
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error Delete POI");
                return StatusCode(500, "Error interno eliminando POI");
            }
        }
    }
}
