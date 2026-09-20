namespace CommuteCompanionApi.Models;

public class DashboardResponse
{
    public int? LocationId { get; set; }

    public string LocationLabel { get; set; } = string.Empty;

    public string LocationAddress { get; set; } = string.Empty;

    public TrafficDashboardData Traffic { get; set; } = new();

    public LoadSheddingDashboardData LoadShedding { get; set; } = new();

    public WeatherDashboardData Weather { get; set; } = new();

    public DateTime RetrievedAt { get; set; } = DateTime.UtcNow;
}

public class TrafficDashboardData
{
    public string Status { get; set; } = "Moderate";

    public int CommuteMinutes { get; set; }

    public int DelayMinutes { get; set; }

    public int IncidentCount { get; set; }
}

public class LoadSheddingDashboardData
{
    public int Stage { get; set; }

    public bool PowerAvailable { get; set; }

    public string ChangeIn { get; set; } = string.Empty;

    public string NextSlot { get; set; } = string.Empty;
}

public class WeatherDashboardData
{
    public double TemperatureCelsius { get; set; }

    public string Condition { get; set; } = string.Empty;

    public bool WetRoads { get; set; }

    public string AlertMessage { get; set; } = string.Empty;
}