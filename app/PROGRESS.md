# GroovyLauncher — Progress Log

## Session 1 — Foundation ✅
- Forked Olauncher on GitHub (crugmere/GroovyLauncher)
- Cloned to local machine, opened in Android Studio
- Renamed package from app.olauncher to app.groovylauncher
- Built and deployed to Samsung S24 Ultra via USB
- Added Cinzel Decorative and Righteous fonts (cinzel_decorative_regular.ttf, righteous_regular.ttf)

## Session 2 — Background ✅
- Created views/GroovyBackgroundView.kt
- Plasma colour morphing background (24x42 grid, sine wave fields)
- 5 lava blobs with RadialGradient, SCREEN blend mode, independent movement
- Wired into fragment_home.xml as bottom layer
- Running at 60fps on S24 via Choreographer

## Session 3 — Vinyl + Mushrooms ✅
- Created views/VinylView.kt — spinning record with grooves, spoke ring, cycling label
- Created views/MushroomView.kt — 5 toadstools with bob animation, tendrils, spot rings
- Both wired into fragment_home.xml
- Hidden first run tips text

## Next — Session 4
- Polish vinyl record visuals (more convincing grooves, darker body)
- Polish mushroom caps (more domed, less jellyfish)
- Fix layout — vinyl position, app names not overlapping
- Begin SplashView with particle system
- First 3 instruments: Cloud Guitar, Gold Top, Reverse Strat
- Session 5:
- Fixed splash centering: missing </LinearLayout> after date TextView in fragment_home.xml
  had nested SplashView inside dateTimeLayout. Removed -height*0.1f hack from SplashView.kt.
- App names: Cinzel Decorative + per-app palette colours + glow via applyGroovyStyle() in HomeFragment.
- Added cloud guitar to splash rotation.
- TODO: instrument PNGs not in GitHub repo — check .gitignore / git add.
- TODO: Cloud Guitar shape may be protected IP (Prince estate) — review before Play Store.
- NEXT: reverse strat + double neck images, app drawer styling, settings.