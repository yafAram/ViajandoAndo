using Microservices.Auth.Api.Data;
using Microservices.Auth.Api.Models;
using Microservices.Auth.Api.Models.Dto;
using Microservices.Auth.Api.Service.IService;
using Microsoft.AspNetCore.Identity;
using Microsoft.IdentityModel.Tokens;

namespace Microservices.Auth.Api.Service
{
    public class AuthService : IAuthService
    {
        private readonly AppDbContext _dbContext;
        private readonly UserManager<ApplicationUser> _userManager;
        private readonly RoleManager<IdentityRole> _roleManager;
        private readonly IJwtTokenGenerator _jwtTokenGenerator;

        public AuthService(
            AppDbContext dbContext,
            UserManager<ApplicationUser> userManager,
            RoleManager<IdentityRole> roleManager,
            IJwtTokenGenerator jwtTokenGenerator)
        {
            _dbContext = dbContext;
            _userManager = userManager;
            _roleManager = roleManager;
            _jwtTokenGenerator = jwtTokenGenerator;
        }

        public async Task<LoginResponseDto> Login(LoginRequestDto loginRequestDto)
        {
            // Corrección: Usar FindByEmailAsync en lugar de FirstOrDefault
            var user = await _userManager.FindByEmailAsync(loginRequestDto.UserName)
                     ?? await _userManager.FindByNameAsync(loginRequestDto.UserName);

            if (user == null || !await _userManager.CheckPasswordAsync(user, loginRequestDto.Password))
            {
                return new LoginResponseDto() { User = null, Token = "" };
            }

            var roles = await _userManager.GetRolesAsync(user);
            var token = _jwtTokenGenerator.GenerateToken(user, roles);

            return new LoginResponseDto()
            {
                User = new UserDto()
                {
                    Email = user.Email,
                    Id = user.Id,
                    Name = user.Name,
                    PhoneNumber = user.PhoneNumber
                },
                Token = token
            };
        }

        // Eliminado: Método RegisterUser duplicado
        public async Task<string> Register(RegistrationRequestDto registrationRequestDto)
        {
            ApplicationUser user = new()
            {
                UserName = registrationRequestDto.Email,
                Email = registrationRequestDto.Email,
                NormalizedEmail = registrationRequestDto.Email.ToUpper(),
                Name = registrationRequestDto.Name,
                PhoneNumber = registrationRequestDto.PhoneNumber
            };

            try
            {
                var result = await _userManager.CreateAsync(user, registrationRequestDto.Password);

                if (result.Succeeded)
                {
                    // 👇 ASIGNACIÓN DE ROL DURANTE EL REGISTRO (NUEVO)
                    if (!string.IsNullOrEmpty(registrationRequestDto.Role))
                    {
                        await AssignRole(registrationRequestDto.Email, registrationRequestDto.Role);
                    }

                    return string.Empty;
                }
                return result.Errors.FirstOrDefault()?.Description ?? "Error desconocido";
            }
            catch (Exception ex)
            {
                return $"Error: {ex.Message}";
            }
        }

        public async Task<bool> AssignRole(string email, string roleName)
        {
            var user = await _userManager.FindByEmailAsync(email);
            if (user == null) return false;

            // Corrección: Usar await en lugar de GetAwaiter().GetResult()
            if (!await _roleManager.RoleExistsAsync(roleName))
            {
                await _roleManager.CreateAsync(new IdentityRole(roleName));
            }

            await _userManager.AddToRoleAsync(user, roleName);
            return true;
        }
    }
}