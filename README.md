# screen_share
Share your desktop screen using a web based client.
Using an embedded web server or a remote web server.

The program [screen_share_remote](https://github.com/rootkiwi/screen_share_remote/)
is used for remote web server screen sharing.

<a href="https://i.imgur.com/Vb0dOvi.png" target="_blank">
<img src="https://i.imgur.com/Vb0dOvi.png"/></a>

## About
I've been working on this program for pretty long now, with a few breaks here and there when I've been busy with
life / school.

It started with sending pure screenshots, using the java Robot class. Calculating the diffs and only sending
the pixels that diffed. It worked but I had to fallback to JPEG when the diffs got to big, otherwise
a bandwidth of >300Mbit/s was needed.

So I started looking into video encoding, and learning about the magic of h264. Using JavaCV to access FFmpeg
to capture the screen and also accessing x264 for encoding. Then Broadway for the awesome h264 javascript decoder.

This is video only and is not made for any high fps stuff like movies / games etc. Just for simple desktop sharing.

Jetty is used for the embedded web server.

## Alpha
I have a lot to do in school at the moment and I feel that I really need to upload this project now,
even though it's not finished. So I can let go of it for a while :)
And make small fixes / implement more stuff when I have time.

The program may contain many bugs as it's not tested that much. I've tested in on Linux with Firefox and Chromium.

## Binaries
Gradle Shadow is used for bundling fat jars, for different platforms.
Right now jars are built for
* Linux x86
* Linux x86_64
* macOS x86_64
* Windows x86
* Windows x86_64

But the program may need a few changes to work on windows / mac, I don't know. Like where the config is stored and
more importantly how the screen is captured.

Download here: [screen_share/releases/latest](https://github.com/rootkiwi/screen_share/releases/latest)

## How to use
Download a jar file for your platform and start it.

Example:
```
java -jar screen_share-0.1.0-linux_x86_64.jar
```

## Dependencies
For Linux x11grab is used for capturing the screen. No Wayland support, no idea if that's possible with FFmpeg.
Anyway `libxcb` is needed for x11grab.

Also if you're using OpenJDK you may need to install openjfx as well.

## License
[GNU General Public License 3 or later](https://www.gnu.org/licenses/gpl-3.0.html)

See LICENSE for more details.

## 3RD Party Dependencies

See also [LICENSES-3RD-PARTY](https://github.com/rootkiwi/screen_share/tree/master/LICENSES-3RD-PARTY).

### Eclipse Jetty

[https://www.eclipse.org/jetty/](https://www.eclipse.org/jetty/)

[Apache License 2.0](https://www.eclipse.org/jetty/licenses.html)


### JavaCV

[https://github.com/bytedeco/javacv/](https://github.com/bytedeco/javacv/)

[Apache License 2.0](https://github.com/bytedeco/javacv/blob/master/LICENSE.txt)


### FFmpeg

[https://ffmpeg.org/](https://ffmpeg.org/)

[GNU General Public License version 2 or later](https://www.ffmpeg.org/legal.html)


### x264

[https://www.videolan.org/developers/x264.html](https://www.videolan.org/developers/x264.html)

[GNU General Public License version 2 or later](https://www.gnu.org/licenses/old-licenses/gpl-2.0.html)


### Broadway

[https://github.com/mbebenita/Broadway/](https://github.com/mbebenita/Broadway/)

[3-clause BSD License](https://github.com/mbebenita/Broadway/blob/master/LICENSE)


### Gradle Shadow

[https://github.com/johnrengelman/shadow/](https://github.com/johnrengelman/shadow/)

[Apache License 2.0](https://github.com/johnrengelman/shadow/blob/master/LICENSE)
