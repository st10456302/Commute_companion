namespace CommuteCompanionApi.Models;

public class NotificationPreferences
{
    public int Id { get; set; }

    public string UserId { get; set; } = string.Empty;

    public bool NotificationsEnabled { get; set; }

    public bool TrafficAlerts { get; set; } = true;

    public bool WeatherAlerts { get; set; } = true;

    public bool LoadSheddingAlerts { get; set; } = true;

    public DateTime UpdatedAt { get; set; } = DateTime.UtcNow;
}