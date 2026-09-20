using CommuteCompanionApi.Data;
using CommuteCompanionApi.Models;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace CommuteCompanionApi.Controllers;

[ApiController]
[Route("api/[controller]")]
public class UsersController : ControllerBase
{
    private readonly AppDbContext _context;

    public UsersController(AppDbContext context)
    {
        _context = context;
    }

    // GET: /api/users/{firebaseUid}
    [HttpGet("{firebaseUid}")]
    public async Task<ActionResult<User>> GetUser(string firebaseUid)
    {
        var user = await _context.Users
            .FirstOrDefaultAsync(x => x.FirebaseUid == firebaseUid);

        if (user == null)
        {
            return NotFound();
        }

        return Ok(user);
    }

    // POST: /api/users
    [HttpPost]
    public async Task<ActionResult<User>> CreateUser(User user)
    {
        var existingUser = await _context.Users
            .FirstOrDefaultAsync(x => x.FirebaseUid == user.FirebaseUid);

        if (existingUser != null)
        {
            return Conflict("A user with this Firebase UID already exists.");
        }

        user.Id = 0;
        user.CreatedAt = DateTime.UtcNow;
        user.UpdatedAt = DateTime.UtcNow;

        _context.Users.Add(user);

        await _context.SaveChangesAsync();

        return CreatedAtAction(
            nameof(GetUser),
            new { firebaseUid = user.FirebaseUid },
            user);
    }

    // PUT: /api/users/{firebaseUid}
    [HttpPut("{firebaseUid}")]
    public async Task<ActionResult<User>> UpdateUser(
        string firebaseUid,
        User updatedUser)
    {
        var existingUser = await _context.Users
            .FirstOrDefaultAsync(x => x.FirebaseUid == firebaseUid);

        if (existingUser == null)
        {
            return NotFound();
        }

        existingUser.Email = updatedUser.Email;
        existingUser.DisplayName = updatedUser.DisplayName;
        existingUser.PreferredLanguage = updatedUser.PreferredLanguage;
        existingUser.UpdatedAt = DateTime.UtcNow;

        await _context.SaveChangesAsync();

        return Ok(existingUser);
    }
}