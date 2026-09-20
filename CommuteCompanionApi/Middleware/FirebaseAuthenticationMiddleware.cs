using FirebaseAdmin.Auth;

namespace CommuteCompanionApi.Middleware;

public class FirebaseAuthenticationMiddleware
{
    private readonly RequestDelegate _next;

    public FirebaseAuthenticationMiddleware(RequestDelegate next)
    {
        _next = next;
    }

    public async Task InvokeAsync(HttpContext context)
    {
        // Allow the health endpoint without authentication.
        if (context.Request.Path.StartsWithSegments("/api/health"))
        {
            await _next(context);
            return;
        }

        // Only protect API endpoints.
        if (!context.Request.Path.StartsWithSegments("/api"))
        {
            await _next(context);
            return;
        }

        // Read the Authorization header.
        var authorizationHeader =
            context.Request.Headers.Authorization.FirstOrDefault();

        if (string.IsNullOrWhiteSpace(authorizationHeader) ||
            !authorizationHeader.StartsWith(
                "Bearer ",
                StringComparison.OrdinalIgnoreCase))
        {
            context.Response.StatusCode = StatusCodes.Status401Unauthorized;

            await context.Response.WriteAsJsonAsync(new
            {
                error = "Authentication required."
            });

            return;
        }

        var idToken = authorizationHeader["Bearer ".Length..].Trim();

        if (string.IsNullOrWhiteSpace(idToken))
        {
            context.Response.StatusCode = StatusCodes.Status401Unauthorized;

            await context.Response.WriteAsJsonAsync(new
            {
                error = "Firebase ID token is missing."
            });

            return;
        }

        try
        {
            // Firebase Admin verifies the token and returns
            // the authenticated user's decoded token.
            var decodedToken =
                await FirebaseAuth.DefaultInstance.VerifyIdTokenAsync(
                    idToken);

            // Make the Firebase UID available to controllers.
            context.Items["FirebaseUid"] = decodedToken.Uid;

            await _next(context);
        }
        catch (FirebaseAuthException)
        {
            context.Response.StatusCode = StatusCodes.Status401Unauthorized;

            await context.Response.WriteAsJsonAsync(new
            {
                error = "Invalid or expired Firebase ID token."
            });
        }
    }
}