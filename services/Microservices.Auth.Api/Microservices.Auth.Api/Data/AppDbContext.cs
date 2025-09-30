using Microservices.Auth.Api.Models;
using Microsoft.AspNetCore.Identity.EntityFrameworkCore;
using Microsoft.AspNetCore.Identity;
using Microsoft.EntityFrameworkCore;

namespace Microservices.Auth.Api.Data
{
    public class AppDbContext : IdentityDbContext <ApplicationUser>
    {
        public AppDbContext(DbContextOptions<AppDbContext> options):base(options) 
        {

        }
        public DbSet<ApplicationUser> ApplicationUsers { get; set; }
        protected override void OnModelCreating(ModelBuilder modelBuilder)
        {
            base.OnModelCreating(modelBuilder);
        }
    }
}
