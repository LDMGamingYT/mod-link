# Mod Link

An innovative way to share mods.

## How does it work?

Upload mods to your server just like normal, but now, players can download the mods directly to their game with the click of **one button!**
No more hassle trying to send the mod files, just get your players to press the `Download Mods` button before connecting!

## Technical Details

This mod uses the UDP protocol to share mods in a **read-only** environment across the internet.

**You should use port `25560` for Mod Link. You can use another port, but Mod Link will NOT be able to find it automatically.**

**TODO:** Change this to the correct documentation if UDP works

### Using another port

Mod Link will **always** use port `25560` internally, but if you wanted to, you can port-forward `25560` to any other port. This is highly discourgaged, as Mod Link will require your players to enter the unqie port every time they want to download the mods.

**To keep this simple, just port-forward `25560` to `25560`. This ensures Mod Link can find it automatically.**
