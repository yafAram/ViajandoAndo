using Microservices.Auth.Api.Models.Dto;
using Microservices.Auth.Api.Service.IService;
using Microsoft.AspNetCore.Mvc;

namespace Microservices.Auth.Api.Controllers.V1
{
    [Route("api/[controller]")]
    [ApiController]
    public class AuthApiController : ControllerBase
    {
        private readonly IAuthService _authService;
        protected ResponseDto _response;

        public AuthApiController(IAuthService authService)
        {
            _authService = authService;
            _response = new();
        }

        [HttpPost("register")]
        public async Task<IActionResult> Register([FromBody] RegistrationRequestDto model)
        {
            var errorMessage = await _authService.Register(model);

            if (!string.IsNullOrEmpty(errorMessage))
            {
                _response.IsSuccess = false;
                _response.Message = errorMessage;
                return BadRequest(_response);
            }
            return Ok(_response);
        }

        [HttpPost("login")]
        public async Task<IActionResult> Login([FromBody] LoginRequestDto model)
        {
            var loginResponse = await _authService.Login(model);

            if (loginResponse.User == null || string.IsNullOrEmpty(loginResponse.Token))
            {
                _response.IsSuccess = false;
                _response.Message = "Credenciales inválidas";
                return Unauthorized(_response); // Cambiado a 401 Unauthorized
            }

            _response.Result = loginResponse;
            return Ok(_response);
        }

        // Corrección: Nombre del método y tipo de DTO
        [HttpPost("AssignRole")]
        public async Task<IActionResult> AssignRole([FromBody] AssignRoleRequestDto model)
        {
            var assignRoleSuccess = await _authService.AssignRole(model.Email, model.Role.ToUpper());

            if (!assignRoleSuccess)
            {
                _response.IsSuccess = false;
                _response.Message = "Error asignando rol";
                return BadRequest(_response);
            }
            return Ok(_response);
        }
    }

}