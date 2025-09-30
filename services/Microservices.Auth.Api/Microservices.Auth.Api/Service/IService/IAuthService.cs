using Microservices.Auth.Api.Models.Dto;

namespace Microservices.Auth.Api.Service.IService
{
    public interface IAuthService
    {
        Task<string> Register(RegistrationRequestDto resgistrationRequesDto);
        Task<LoginResponseDto> Login(LoginRequestDto loginRequestDto);
        Task<bool> AssignRole(string email, string roleName);
    }
}
