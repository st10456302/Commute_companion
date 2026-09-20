using CommuteCompanionApi.Data;
using CommuteCompanionApi.Models;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace CommuteCompanionApi.Controllers;

[ApiController]
[Route("api/users")]
public class UsersController : ControllerBase
{
    private readonly AppDbContext _context;

    public UsersController(AppDbContext context)
    {
        _context = context;
    }

    // GET: /api/users/me
    //
    // Returns the currently authenticated Firebase user's
    // profile from the local application database.
    [HttpGet("me")]
    public async Task<ActionResult<User>> GetCurrentUser()
    {
        var firebaseUid = GetFirebaseUid();

        if (firebaseUid == null)
        {
            return Unauthorized(new
            {
                error = "Authenticated Firebase user was not found."
            });
        }

        var user = await _context.Users
            .FirstOrDefaultAsync(x => x.FirebaseUid == firebaseUid);

        if (user == null)
        {
            return NotFound(new
            {
                error = "User profile does not exist in the application database."
            });
        }

        return Ok(user);
    }

    // POST: /api/users/me
    //
    // Creates the application user profile using the Firebase UID
    // from the verified authentication token.
    [HttpPost("me")]
    public async Task<ActionResult<User>> CreateCurrentUser(
        [FromBody] UserProfileRequest request)
    {
        var firebaseUid = GetFirebaseUid();

        if (firebaseUid == null)
        {
            return Unauthorized(new
            {
                error = "Authenticated Firebase user was not found."
            });
        }

        var existingUser = await _context.Users
            .FirstOrDefaultAsync(x => x.FirebaseUid == firebaseUid);

        if (existingUser != null)
        {
            return Conflict(new
            {
                error = "A user profile already exists."
            });
        }

        var user = new User
        {
            FirebaseUid = firebaseUid,
            Email = request.Email ?? string.Empty,
            DisplayName = request.DisplayName ?? string.Empty,
            PreferredLanguage = request.PreferredLanguage ?? "English",
            CreatedAt = DateTime.UtcNow,
            UpdatedAt = DateTime.UtcNow
        };

        _context.Users.Add(user);

        await _context.SaveChangesAsync();

        return CreatedAtAction(
            nameof(GetCurrentUser),
            null,
            user);
    }

    // PUT: /api/users/me
    //
    // Updates the currently authenticated user's profile.
    [HttpPut("me")]
    public async Task<ActionResult<User>> UpdateCurrentUser(
        [FromBody] UserProfileRequest request)
    {
        var firebaseUid = GetFirebaseUid();

        if (firebaseUid == null)
        {
            return Unauthorized(new
            {
                error = "Authenticated Firebase user was not found."
            });
        }

        var existingUser = await _context.Users
            .FirstOrDefaultAsync(x => x.FirebaseUid == firebaseUid);

        if (existingUser == null)
        {
            return NotFound(new
            {
                error = "User profile does not exist."
            });
        }

        if (request.Email != null)
        {
            existingUser.Email = request.Email;
        }

        if (request.DisplayName != null)
        {
            existingUser.DisplayName = request.DisplayName;
        }

        if (request.PreferredLanguage != null)
        {
            existingUser.PreferredLanguage =
                request.PreferredLanguage;
        }

        existingUser.UpdatedAt = DateTime.UtcNow;

        await _context.SaveChangesAsync();

        return Ok(existingUser);
    }

    private string? GetFirebaseUid()
    {
        return HttpContext.Items["FirebaseUid"] as string;
    }
}

public class UserProfileRequest
{
    public string? Email { get; set; }

    public string? DisplayName { get; set; }

    public string? PreferredLanguage { get; set; }
}