# API Setup — API Keys, JSON Configuration and Mapbox

Follow these steps before running the **CommuteCompanionApi** backend.

> **Important:** API keys are personal secrets. Do **not** commit API keys, passwords, or secret tokens to GitHub. Each team member should configure their own local environment.

---

## 1. Open the API Project

Open the Android Studio Terminal and select **PowerShell**.

From the project root, enter the backend project:

```powershell
cd .\CommuteCompanionApi
```

You should now be inside the `CommuteCompanionApi` folder.

---

# 2. Add the Three API Keys

The backend uses three external APIs:

* OpenWeather — weather and geocoding-related services
* TomTom — traffic flow, incidents and routing
* EskomSePush — load-shedding information

Each team member must add their own API keys to their local PowerShell session.

---

## 2.1 Add the OpenWeather API Key

Replace `YOUR_OPENWEATHER_KEY` with the OpenWeather API key provided to the group.

```powershell
$env:OPENWEATHER_API_KEY="YOUR_OPENWEATHER_KEY"
```

Check that the key has been loaded without displaying the actual secret:

```powershell
if ($env:OPENWEATHER_API_KEY) { "OPENWEATHER_API_KEY = SET" } else { "OPENWEATHER_API_KEY = NOT SET" }
```

Expected result:

```text
OPENWEATHER_API_KEY = SET
```

---

## 2.2 Add the TomTom API Key

Replace `YOUR_TOMTOM_KEY` with the TomTom API key provided to the group.

```powershell
$env:TOMTOM_API_KEY="YOUR_TOMTOM_KEY"
```

Check it safely:

```powershell
if ($env:TOMTOM_API_KEY) { "TOMTOM_API_KEY = SET" } else { "TOMTOM_API_KEY = NOT SET" }
```

Expected result:

```text
TOMTOM_API_KEY = SET
```

---

## 2.3 Add the EskomSePush API Key

Replace `YOUR_ESKOMSEPUSH_KEY` with the EskomSePush API key provided to the group.

```powershell
$env:ESKOMSEPUSH_API_KEY="YOUR_ESKOMSEPUSH_KEY"
```

Check it safely:

```powershell
if ($env:ESKOMSEPUSH_API_KEY) { "ESKOMSEPUSH_API_KEY = SET" } else { "ESKOMSEPUSH_API_KEY = NOT SET" }
```

Expected result:

```text
ESKOMSEPUSH_API_KEY = SET
```

---

## 2.4 Confirm All Three Keys

Run all three checks:

```powershell
if ($env:OPENWEATHER_API_KEY) { "OPENWEATHER_API_KEY = SET" } else { "OPENWEATHER_API_KEY = NOT SET" }

if ($env:TOMTOM_API_KEY) { "TOMTOM_API_KEY = SET" } else { "TOMTOM_API_KEY = NOT SET" }

if ($env:ESKOMSEPUSH_API_KEY) { "ESKOMSEPUSH_API_KEY = SET" } else { "ESKOMSEPUSH_API_KEY = NOT SET" }
```

You should see:

```text
OPENWEATHER_API_KEY = SET
TOMTOM_API_KEY = SET
ESKOMSEPUSH_API_KEY = SET
```

---

# 3. Add the Group JSON Configuration File

The project also requires the **JSON configuration file provided in the group chat/shared project files**.

### Step 1

Locate the JSON file that was provided to the group.

### Step 2

Copy the JSON file into the API project in the **same folder/path used by the existing backend configuration**.

The final structure should look similar to:

```text
Commute_companion_app/
│
├── app/
│
├── CommuteCompanionApi/
│   ├── Controllers/
│   ├── Services/
│   ├── Models/
│   ├── Properties/
│   ├── Program.cs
│   ├── appsettings.json
│   ├── [GROUP JSON FILE]
│   └── ...
│
└── ...
```

> **Important:** Use the exact filename and folder location provided with the project. Do not rename the JSON file unless the code has also been configured to use the new name.

### Step 3

If the JSON file contains credentials, tokens or other secrets:

* Do **not** commit the file to GitHub unless the team has specifically confirmed that it contains no secrets.
* Do not paste the contents into public repositories.
* Keep a local copy on your development machine.
* If the project already contains a safe/example version of the JSON file, use that as the template.

---

# 4. Add the Mapbox Access Token

The Android application uses **Mapbox** for the location map.

Create the following file inside the Android project:

```text
app
└── src
    └── main
        └── res
            └── values
                └── mapbox_access_token.xml
```

If `mapbox_access_token.xml` does not exist, right-click the `values` folder and create a new **Values Resource File** named:

```text
mapbox_access_token.xml
```

Add the Mapbox token provided to the group.

The file should follow this structure:

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>

    <string name="mapbox_access_token" translatable="false">YOUR_MAPBOX_TOKEN</string>

</resources>
```

Replace:

```text
YOUR_MAPBOX_TOKEN
```

with the Mapbox access token provided to the group.

### Important

The Mapbox token file is a local configuration file and should **not be committed to GitHub** if it contains a private/secret token.

The project `.gitignore` should contain:

```gitignore
app/src/main/res/values/mapbox_access_token.xml
```

---

# 5. Start the API

After setting the environment variables, make sure you are still inside:

```text
CommuteCompanionApi
```

Run the API using:

```powershell
dotnet run
```

The terminal should display the address where the API is running.

For example:

```text
Now listening on: http://localhost:XXXX
```

Keep this terminal running while testing the Android application.

---

# 6. Verify the API

Once the API is running, open the API health endpoint in a browser or use the project's Swagger interface.

The health endpoint is:

```text
/api/health
```

A successful response should indicate that the API is healthy.

Swagger can also be used to test the available backend endpoints.

---

# 7. Important: Restart the Terminal When Necessary

The `$env:` commands set environment variables for the **current PowerShell session**.

If you close the terminal and open a new one, you may need to set the three variables again:

```powershell
$env:OPENWEATHER_API_KEY="YOUR_OPENWEATHER_KEY"
$env:TOMTOM_API_KEY="YOUR_TOMTOM_KEY"
$env:ESKOMSEPUSH_API_KEY="YOUR_ESKOMSEPUSH_KEY"
```

Then verify:

```powershell
if ($env:OPENWEATHER_API_KEY) { "OPENWEATHER_API_KEY = SET" } else { "OPENWEATHER_API_KEY = NOT SET" }

if ($env:TOMTOM_API_KEY) { "TOMTOM_API_KEY = SET" } else { "TOMTOM_API_KEY = NOT SET" }

if ($env:ESKOMSEPUSH_API_KEY) { "ESKOMSEPUSH_API_KEY = SET" } else { "ESKOMSEPUSH_API_KEY = NOT SET" }
```

---

# 8. Troubleshooting

### API says an API key is missing

Run the three verification commands again.

If one returns:

```text
NOT SET
```

set that environment variable again.

---

### API starts but weather does not work

Check:

```text
OPENWEATHER_API_KEY
```

and confirm that the OpenWeather key has been entered correctly.

---

### Traffic information does not work

Check:

```text
TOMTOM_API_KEY
```

and confirm that the TomTom key has been entered correctly.

---

### Load-shedding information does not work

Check:

```text
ESKOMSEPUSH_API_KEY
```

and confirm that the EskomSePush key has been entered correctly.

---

### Map does not load in Android

Check that:

```text
app/src/main/res/values/mapbox_access_token.xml
```

exists and contains the correct Mapbox access token.

Also make sure the file is not accidentally named:

```text
mapbox_access_token.xml.txt
```

---

# 9. Before Committing to GitHub

Before pushing changes, make sure you have **not accidentally committed any secrets**.

Do not commit:

```text
API keys
Passwords
Private OAuth credentials
Secret tokens
Private configuration files
```

The following local files should remain ignored where applicable:

```gitignore
app/src/main/res/values/mapbox_access_token.xml

CommuteCompanionApi/bin/
CommuteCompanionApi/obj/
CommuteCompanionApi/*.db
CommuteCompanionApi/*.db-shm
CommuteCompanionApi/*.db-wal
```

The source code should retrieve API keys from environment variables rather than storing the actual keys directly in the C# source code.

---

# Quick Setup Checklist

Before running the application, each team member should have:

* [ ] Cloned/pulled the latest GitHub repository
* [ ] Added the required group JSON configuration file
* [ ] Added `OPENWEATHER_API_KEY`
* [ ] Added `TOMTOM_API_KEY`
* [ ] Added `ESKOMSEPUSH_API_KEY`
* [ ] Confirmed all three variables show `SET`
* [ ] Added `mapbox_access_token.xml` locally
* [ ] Added the correct Mapbox token
* [ ] Started `CommuteCompanionApi` with `dotnet run`
* [ ] Confirmed `/api/health` works
* [ ] Opened the Android application
* [ ] Confirmed the app can communicate with the backend

Once these steps are completed, the backend and Android application should have the required local configuration to run the Commute Companion API integrations.
