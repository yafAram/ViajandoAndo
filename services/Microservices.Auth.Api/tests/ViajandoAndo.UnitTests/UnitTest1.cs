using System.Threading.Tasks;
using FluentAssertions;
using Microsoft.AspNetCore.Mvc;
using Moq;
using Xunit;
using Microservices.Auth.Api.Controllers.V1;
using Microservices.Auth.Api.Models.Dto;
using Microservices.Auth.Api.Service.IService;


namespace ViajandoAndo.UnitTests;

public class UnitTest1
{
    public class AuthApiControllerTests
    {
        private readonly Mock<IAuthService> _authServiceMock;
        private readonly AuthApiController _controller;

        public AuthApiControllerTests()
        {
            _authServiceMock = new Mock<IAuthService>();
            _controller = new AuthApiController(_authServiceMock.Object);
        }

        #region Register

        [Fact]
        public async Task Register_Returns_BadRequest_When_ServiceReturnsError()
        {
            // Arrange
            var request = new RegistrationRequestDto
            {
                Email = "test@example.com",
                Name = "Test",
                PhoneNumber = "123456",
                Password = "P@ssw0rd",
                Role = "USER"
            };

            var errorMessage = "El correo ya existe";
            _authServiceMock
                .Setup(s => s.Register(It.IsAny<RegistrationRequestDto>()))
                .ReturnsAsync(errorMessage);

            // Act
            var result = await _controller.Register(request);

            // Assert
            result.Should().BeOfType<BadRequestObjectResult>();
            var bad = result as BadRequestObjectResult;
            bad.Value.Should().BeOfType<ResponseDto>();
            var resp = bad.Value as ResponseDto;
            resp.IsSuccess.Should().BeFalse();
            resp.Message.Should().Be(errorMessage);

            _authServiceMock.Verify(s => s.Register(It.IsAny<RegistrationRequestDto>()), Times.Once);
        }

        [Fact]
        public async Task Register_Returns_Ok_When_ServiceReturnsNoError()
        {
            // Arrange
            var request = new RegistrationRequestDto
            {
                Email = "new@example.com",
                Name = "New",
                PhoneNumber = "000",
                Password = "P@ss",
                Role = "USER"
            };

            _authServiceMock
                .Setup(s => s.Register(It.IsAny<RegistrationRequestDto>()))
                .ReturnsAsync(string.Empty); // sin error

            // Act
            var result = await _controller.Register(request);

            // Assert
            result.Should().BeOfType<OkObjectResult>();
            var ok = result as OkObjectResult;
            ok.Value.Should().BeOfType<ResponseDto>();
            var resp = ok.Value as ResponseDto;
            resp.IsSuccess.Should().BeTrue();
            resp.Message.Should().BeNullOrEmpty();

            _authServiceMock.Verify(s => s.Register(It.IsAny<RegistrationRequestDto>()), Times.Once);
        }

        #endregion

        #region Login

        [Fact]
        public async Task Login_Returns_Unauthorized_When_UserNullOrTokenEmpty()
        {
            // Arrange: caso User null
            var requestNullUser = new LoginRequestDto { UserName = "u", Password = "p" };
            _authServiceMock
                .Setup(s => s.Login(It.IsAny<LoginRequestDto>()))
                .ReturnsAsync(new LoginResponseDto { User = null, Token = "" });

            // Act
            var result = await _controller.Login(requestNullUser);

            // Assert
            result.Should().BeOfType<UnauthorizedObjectResult>();
            var unauth = result as UnauthorizedObjectResult;
            unauth.Value.Should().BeOfType<ResponseDto>();
            var resp = unauth.Value as ResponseDto;
            resp.IsSuccess.Should().BeFalse();
            resp.Message.Should().Be("Credenciales inválidas");

            _authServiceMock.Verify(s => s.Login(It.IsAny<LoginRequestDto>()), Times.Once);
        }

        [Fact]
        public async Task Login_Returns_Ok_With_Result_When_Success()
        {
            // Arrange
            var request = new LoginRequestDto { UserName = "user", Password = "pwd" };
            var expected = new LoginResponseDto
            {
                User = new UserDto { /* setea propiedades mínimas si hace falta */ },
                Token = "token123"
            };

            _authServiceMock
                .Setup(s => s.Login(It.IsAny<LoginRequestDto>()))
                .ReturnsAsync(expected);

            // Act
            var result = await _controller.Login(request);

            // Assert
            result.Should().BeOfType<OkObjectResult>();
            var ok = result as OkObjectResult;
            ok.Value.Should().BeOfType<ResponseDto>();
            var resp = ok.Value as ResponseDto;
            resp.IsSuccess.Should().BeTrue();
            resp.Result.Should().BeEquivalentTo(expected);

            _authServiceMock.Verify(s => s.Login(It.IsAny<LoginRequestDto>()), Times.Once);
        }

        #endregion

        #region AssignRole

        [Fact]
        public async Task AssignRole_Returns_BadRequest_When_ServiceReturnsFalse()
        {
            // Arrange
            var req = new AssignRoleRequestDto { Email = "a@ex.com", Role = "user" };
            _authServiceMock
                .Setup(s => s.AssignRole(It.IsAny<string>(), It.IsAny<string>()))
                .ReturnsAsync(false);

            // Act
            var result = await _controller.AssignRole(req);

            // Assert
            result.Should().BeOfType<BadRequestObjectResult>();
            var bad = result as BadRequestObjectResult;
            bad.Value.Should().BeOfType<ResponseDto>();
            var resp = bad.Value as ResponseDto;
            resp.IsSuccess.Should().BeFalse();
            resp.Message.Should().Be("Error asignando rol");

            _authServiceMock.Verify(s => s.AssignRole(req.Email, req.Role.ToUpper()), Times.Once);
        }

        [Fact]
        public async Task AssignRole_Returns_Ok_And_CallsServiceWithUppercaseRole_When_ServiceReturnsTrue()
        {
            // Arrange
            var req = new AssignRoleRequestDto { Email = "b@ex.com", Role = "admin" };
            _authServiceMock
                .Setup(s => s.AssignRole(It.IsAny<string>(), It.IsAny<string>()))
                .ReturnsAsync(true);

            // Act
            var result = await _controller.AssignRole(req);

            // Assert
            result.Should().BeOfType<OkObjectResult>();
            var ok = result as OkObjectResult;
            ok.Value.Should().BeOfType<ResponseDto>();
            var resp = ok.Value as ResponseDto;
            resp.IsSuccess.Should().BeTrue();

            // Verificamos que el servicio fue llamado con Role en mayúsculas
            _authServiceMock.Verify(s => s.AssignRole(req.Email, req.Role.ToUpper()), Times.Once);
        }

        #endregion
    }
}
