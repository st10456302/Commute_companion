using CommuteCompanionApi.Models;
using Microsoft.EntityFrameworkCore;

namespace CommuteCompanionApi.Data;

public class AppDbContext : DbContext
{
    public AppDbContext(DbContextOptions<AppDbContext> options)
        : base(options)
    {
    }

    public DbSet<User> Users => Set<User>();

    public DbSet<SavedLocation> SavedLocations => Set<SavedLocation>();

    public DbSet<NotificationPreferences> NotificationPreferences =>
        Set<NotificationPreferences>();
}