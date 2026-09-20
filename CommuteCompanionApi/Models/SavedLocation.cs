namespace CommuteCompanionApi.Models;

public class SavedLocation
{
    public int Id { get; set; }

    public string UserId { get; set; } = string.Empty;

    public string Label { get; set; } = string.Empty;

    public string Address { get; set; } = string.Empty;

    public double? Latitude { get; set; }

    public double? Longitude { get; set; }

    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
}