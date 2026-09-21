using CommuteCompanionApi.Data;
using FirebaseAdmin;
using Google.Apis.Auth.OAuth2;
using Microsoft.EntityFrameworkCore;
using CommuteCompanionApi.Middleware;
using CommuteCompanionApi.Services;

var builder = WebApplication.CreateBuilder(args);

// Add controller support.
builder.Services.AddControllers();

// Register OpenWeather geocoding service.
builder.Services.AddHttpClient<OpenWeatherGeocodingService>();

// Register OpenWeather weather service.
builder.Services.AddHttpClient<WeatherService>();

// Register TomTom traffic service.
builder.Services.AddHttpClient<TomTomTrafficService>();

//eskom loadshedding service
builder.Services.AddHttpClient<EskomSePushService>();

// Register Entity Framework Core with SQLite.
builder.Services.AddDbContext<AppDbContext>(options =>
    options.UseSqlite(
        builder.Configuration.GetConnectionString("DefaultConnection")
    ));

// Initialize Firebase Admin SDK.
// The SDK reads the service-account path from
// GOOGLE_APPLICATION_CREDENTIALS.
FirebaseApp.Create(new AppOptions
{
    Credential = GoogleCredential.GetApplicationDefault()
});

// Add Swagger/OpenAPI services.
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen();

var app = builder.Build();
app.UseMiddleware<FirebaseAuthenticationMiddleware>();

// Enable Swagger in the development environment.
if (app.Environment.IsDevelopment())
{
    app.UseSwagger();
    app.UseSwaggerUI();
}

// Enable controller endpoints.
app.MapControllers();

// Basic API health check.
app.MapGet("/api/health", () =>
{
    return Results.Ok(new
    {
        status = "healthy",
        service = "Commute Companion API",
        timestamp = DateTime.UtcNow
    });
});

app.Run();