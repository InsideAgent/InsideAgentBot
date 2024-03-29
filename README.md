<div id="top"></div>



<!-- PROJECT SHIELDS -->
<!--
*** I'm using markdown "reference style" links for readability.
*** Reference links are enclosed in brackets [ ] instead of parentheses ( ).
*** See the bottom of this document for the declaration of the reference variables
*** for contributors-url, forks-url, etc. This is an optional, concise syntax you may use.
*** https://www.markdownguide.org/basic-syntax/#reference-style-links
-->
<!-- [![Contributors][contributors-shield]][contributors-url] -->
[![Forks][forks-shield]][forks-url]
[![Stargazers][stars-shield]][stars-url]
[![Issues][issues-shield]][issues-url]
[![GitHub release (latest by date including pre-releases)][releases-shield]][releases-url]
[![GitHub commit activity][commits-shield]][commits-url]



<!-- PROJECT LOGO -->
<br />
<div align="center">
  <a href="https://github.com/InsideAgent/DiscordBot">
    <img src="src/main/resources/images/InsideAgent-New-Blue.png" alt="Logo" width="80" height="80">
  </a>

<h3 align="center">Inside Agent Bot</h3>

  <p align="center">
    Discord Audio Player & More.
    <br />
    <a href="https://docs.insideagent.pro"><strong>Explore the docs »</strong></a>
    <br />
    <br />
    <a href="https://discord.com/api/oauth2/authorize?client_id=786721755560804373&permissions=8&scope=bot">View Demo</a>
    ·
    <a href="https://github.com/InsideAgent/DiscordBot/issues">Report Bug</a>
    ·
    <a href="https://github.com/InsideAgent/DiscordBot/issues">Request Feature</a>
  </p>
</div>



<!-- TABLE OF CONTENTS -->
<details>
  <summary>Table of Contents</summary>
  <ol>
    <li>
      <a href="#about-the-project">About The Project</a>
      <ul>
        <li><a href="#built-with">Built With</a></li>
      </ul>
    </li>
    <li>
      <a href="#getting-started">Getting Started</a>
    </li>
    <li><a href="#usage">Usage</a></li>
    <li><a href="#roadmap">Roadmap</a></li>
    <li><a href="#contributing">Contributing</a></li>
    <li><a href="#license">License</a></li>
    <li><a href="#contact">Contact</a></li>
    <li><a href="#acknowledgments">Acknowledgments</a></li>
  </ol>
</details>



<!-- ABOUT THE PROJECT -->
## About The Project

This project started as a personal challenge/venture, but when I realized how much it helped my close friends discord server. I decided I wanted to make the project more professional, and above all, OPEN SOURCE!

<p align="right">(<a href="#top">back to top</a>)</p>



### Built With

* [Java](https://java.com/)
* [JDA-Discord](https://github.com/DV8FromTheWorld/JDA)
* [LavaPlayer](https://github.com/sedmelluq/lavaplayer)
* [LavaPlayer-Fork](https://github.com/Walkyst/lavaplayer-fork)
* [Java-MySQL](https://mvnrepository.com/artifact/mysql/mysql-connector-java)
* [Icecast](https://icecast.org/)

<p align="right">(<a href="#top">back to top</a>)</p>



<!-- GETTING STARTED -->
## Getting Started

To add this bot to your own discord server, simply click this:

[![Add To Server][install]][install-link]


and follow the steps to add it to the server you want.

And just like that the bot is ready to go!

<p align="right">(<a href="#top">back to top</a>)</p>



<!-- USAGE EXAMPLES -->
## Usage

### Current Command List for AudioPlayer:
_(Note Current commands with '-' are deprecated and should use slash commands instead)_
- `/play` - Add a link to most streaming platforms, or use its name to search!
- `/skip` - Skips the current song!
- `/volume` - a number 1-100 to adjust volume!
- `/clear` - clears the current queue!
- `/stop` or - `/pause` - pauses the current playing audio!
- `/resume` - resumes the current queue!
- `/disconnect` or - `/dc` or - `/leave` - disconnects the bot from its channel!
- `/follow` -  moves the bot to your current channel!
- `/queue` or - `/q`  - Shows a Embed of songs (10 per page) with page selectors, and a button to remove the message!
- `/shuffle` - Shuffles the queue!
- `/song` or - `/info` - Shows info about the song, including a progress bar, the song requester, and Title/Author!
- `/remove` - Removes a song from the queue at a given index number!
- `/seek` - Takes in arg in the form of `HH:mm:ss` that seeks to that time in the current song!
- `/fix` - Resets the VoiceChannel's region to help reduce latency and audio lag.
- `/loop` - Args are either `song` or `queue` to loop a song or to loop the entire queue! (_Skipping resets any loops!_)
- `/move` - Intakes to numbers and swaps their positions in the queue!
- `/hijack` - Secret Command 🤫**Only Accessible by certified DJ's!**
- `/playtop` or - `/ptop` - Adds a song to the top of the queue.
- `/skipto` or - `/st` - Skips to the given index in the queue.
- `/fileplay` - Plays a given file (inserted upon command entered).
- `/move` - Moves a given track to a given index in the queue
- `/radio` - Adds a playlist of songs from chosen genres.

*For more examples, please refer to the Docs: COMING SOON*


## Embed Builder

While sitting one day, I wished there was an easy way to create embed's from the discord client. I learned of `Carl Bot` and it's web-editor, but I wanted something BUILT-IN to discord, so I created the Embed editor.

To Start, use the `slash` command `/embedbuilder (channel)` (the channel arg being optional):

![image](https://user-images.githubusercontent.com/69219325/181678880-7f316743-0b28-4edd-bf8e-9c2da3c86305.png)

Next it will give a request button looking like this:

![image](https://user-images.githubusercontent.com/69219325/181678946-b0d3202f-36c5-4867-b93b-3576d8d1a62a.png)

After clicking the button you will be greeted with the default embed, and a drop down list of options to edit the embed along with the `Send!` button, to send the embed to the specified channel:

![image](https://user-images.githubusercontent.com/69219325/181679023-629fed43-14b2-4b6e-be14-f2d5e6d9daa5.png)

*Note: When using the `color` function, be sure to format the color with hex integer format (`0xdbbd25`, `0x + 6_Hex_Digits`)*

![image](https://user-images.githubusercontent.com/69219325/181679182-881d1589-910e-45ed-9a48-4ce6f0a2481e.png)

*Note: The author field supports linking in the text*

![image](https://user-images.githubusercontent.com/69219325/181680075-880352ea-54c4-4dcd-a250-c4bc95fb99a2.png)

*Note: Most text inputs support Markdown synatax!*

![image](https://user-images.githubusercontent.com/69219325/181680128-d4c011c8-37aa-4f90-b3e6-b1a0da469315.png)

And Finally upon sending your embed, you get a result like this:

![image](https://user-images.githubusercontent.com/69219325/181680403-0abeee6c-d195-4678-b743-58aae664e134.png)

There are many other nuances and cool things to learn about the embed editor, but I'm not going to list them here, so go find them yourself!
<p align="right">(<a href="#top">back to top</a>)</p>



<!-- ROADMAP -->
## Roadmap

- [x] ~~Complete Core AudioPlayer Features~~
- [x] ~~Add supplemental features to AudioPlayer~~
- [ ] ~~Complete GameSpy~~ *(Removed Pre-0.1.9)*
    - [ ] ~~Log specific games, and give weekly reports~~
- [x] ~~Currently obtaining server to achieve near 100% uptime.~~
- [x] New Features, feature requests.
- [ ] Expand the roadmap.

See the [open issues](https://github.com/InsideAgent/DiscordBot/issues) for a full list of proposed features (and known issues).

<p align="right">(<a href="#top">back to top</a>)</p>



<!-- CONTRIBUTING -->
## Contributing

Contributions are what make the open source community such an amazing place to learn, inspire, and create. Any contributions you make are **greatly appreciated**.

If you have a suggestion that would make this better, please fork the repo and create a pull request. You can also simply open an issue with the tag "enhancement".
I will be actively reading requests, and updating the project with new features, so please do make requests if you can think of any features you would like to see.

Don't forget to give the project a star! Thanks again!

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

<p align="right">(<a href="#top">back to top</a>)</p>



<!-- LICENSE -->
## License

Distributed under the GNU Affero General Public License v3.0. See `LICENSE.txt` for more information.

<p align="right">(<a href="#top">back to top</a>)</p>



<!-- CONTACT -->
## Contact

Jacrispys - [@JacrispysDev](https://twitter.com/JacrispyDev) - jacrispysyt@gmail.com

Project Link: [https://github.com/InsideAgent/DiscordBot](https://github.com/InsideAgent/DiscordBot)

<p align="right">(<a href="#top">back to top</a>)</p>



<!-- ACKNOWLEDGMENTS -->
## Acknowledgments

* [Jacrispys - Lead Designer and Creator](https://github.com/Jacrispys)

<p align="right">(<a href="#top">back to top</a>)</p>



<!-- MARKDOWN LINKS & IMAGES -->
<!-- https://www.markdownguide.org/basic-syntax/#reference-style-links -->
[contributors-shield]: https://img.shields.io/github/contributors/InsideAgent/DiscordBot.svg?style=for-the-badge
[contributors-url]: https://github.com/InsideAgent/DiscordBot/graphs/contributors
[forks-shield]: https://img.shields.io/github/forks/InsideAgent/DiscordBot.svg?style=for-the-badge
[forks-url]: https://github.com/InsideAgent/DiscordBot/network/members
[stars-shield]: https://img.shields.io/github/stars/InsideAgent/DiscordBot.svg?style=for-the-badge
[stars-url]: https://github.com/InsideAgent/DiscordBot/stargazers
[issues-shield]: https://img.shields.io/github/issues/InsideAgent/DiscordBot.svg?style=for-the-badge
[issues-url]: https://github.com/InsideAgent/DiscordBot/issues
[license-shield]: https://img.shields.io/github/license/InsideAgent/DiscordBot.svg?style=for-the-badge
[license-url]: https://github.com/InsideAgent/DiscordBot/blob/master/LICENSE.txt
[releases-shield]: https://img.shields.io/github/v/release/InsideAgent/DiscordBot?include_prereleases&style=for-the-badge
[releases-url]: https://github.com/InsideAgent/DiscordBot/releases
[commits-shield]: https://img.shields.io/github/commit-activity/m/InsideAgent/DiscordBot?style=for-the-badge
[commits-url]: https://github.com/InsideAgent/DiscordBot/commits/master
[product-screenshot]: images/screenshot.png
[install]: https://img.shields.io/badge/-%3E%20Add%20To%20Server%20%3C-%234287f5?style=for-the-badge
[install-link]: https://discord.com/api/oauth2/authorize?client_id=786721755560804373&permissions=8&scope=bot%20applications.commands

