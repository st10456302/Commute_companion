namespace CommuteCompanionApi.Models;

public class SavedRoute
{
    public int Id { get; set; }

    public string UserId { get; set; } = string.Empty;

    public string Name { get; set; } = string.Empty;

    public string Destination { get; set; } = string.Empty;

    public double? DestinationLatitude { get; set; }

    public double? DestinationLongitude { get; set; }

    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
}