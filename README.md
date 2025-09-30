# MSMSandbox-Master
##### This readme was made with the assistance of generative AI. The code was not.

This repository contains the **full code** for the MSM Sandbox, a private server project I developed between January and July 2025.

> [!NOTE]
> This is the code specifically for the sfs2x server. The pregame setup and auth server code is not included in this repository.

## What is MSM Sandbox?
MSM Sandbox is a private server project of My Singing Monsters. It was first created in mid-2024 and became the first private server for the PC version of the game.

In January 2025, I began fully re-coding MSM Sandbox to improve stability, add new features, and make it easier to expand in the future.

The rewrite introduces new systems that make the project much more organized and stable, which includes:

* Custom classes for handling players, islands, monsters, and structures.
* Database support (hosted on PythonAnywhere) for storing player data and game state.
* Cleaner architecture and easier-to-expand codebase compared to the original 2024 build.

However, in July 2025, My Singing Monsters updated its client to connect through an HTTP server using WebSockets. Since the MSM Sandbox recode was written in Java, and no reliable WebSocket modules were available for this setup, the server could no longer function. This forced the shutdown of MSM Sandbox, bringing an end to the project and leaving behind a community of players who had been enjoying it.

## Run locally

Requirements:

* Java 8 or higher
* SmartFoxServer2X installed and running

Steps to run the server:

1. Clone this repository to your local machine.
2. Configure SmartFoxServer2X to point to the project directory.

3. Set up the database connection:

By default, the server communicates with a remote PythonAnywhere database.

For local testing, you can configure the server to connect to a local database instance.

> [!IMPORTANT]
> The current HTTP-based database interface is vulnerable to SQL injection with Monster names and player display names.

4. Start SmartFoxServer2X and run the MSM Sandbox server code.

Notes for contributors:

* Focus on adding WebSocket support or fixing bugs first.
* Pull requests and bug reports are welcome, but make sure to test changes locally before submitting.

## Contributing

MSM Sandbox was originally shut down in July 2025 due to My Singing Monsters switching to an HTTP + WebSocket client connection. The current codebase is built on SmartFoxServer2X (Java), which has no reliable WebSocket support out of the box.

If you'd like to contribute, here are a few areas that would greatly help the project:

- [ ] Implementing or integrating WebSocket support into the SmartFoxServer2X backend, fixing the shutdown issue entirely.
- [ ] Fixing bugs or stability issues in the custom classes.
- [ ] Improving database security and reliability, currently the server communicates with a remote database via HTTP requests (for example, {'password': 'db-password', 'command': 'command'}) and is vulnerable to SQL injection.
