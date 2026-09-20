using Microsoft.AspNetCore.Mvc;

namespace CommuteCompanionApi.Controllers;

[ApiController]
[Route("api/[controller]")]
public class AuthController : ControllerBase
{
    // GET: /api/auth/me
    //
    // This endpoint returns information about the Firebase user
    // authenticated by FirebaseAuthenticationMiddleware.
    [HttpGet("me")]
    public IActionResult GetCurrentUser()
    {
        var firebaseUid = HttpContext.Items["FirebaseUid"] as string;

        if (string.IsNullOrWhiteSpace(firebaseUid))
        {
            return Unauthorized(new
            {
                error = "Authenticated Firebase user was not found."
            });
        }

        return Ok(new
        {
            firebaseUid = firebaseUid
        });
    }
}