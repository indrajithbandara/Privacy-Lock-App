# Physical-device acceptance checklist

Status: not executed on physical hardware. Use an owner-authorized test device with a non-sensitive fixture application and record the exact build SHA, device model, Android version, signing certificate, locale, and result for each case. Never use real credentials or personal app data in screenshots or logs.

## Functional and lifecycle checks

- Install fresh, launch, enroll a new PIN and confirm explicit accessibility disclosure. Enable the service using Android settings. Verify the dashboard accurately reports service availability.
- Select a non-system fixture application. Open it from the launcher, notification and deep link; verify the authentication prompt, incorrect PIN rejection and a corresponding failed authentication record. Authenticate correctly and verify normal fixture access.
- Switch between protected and unprotected fixture apps; verify immediate mode, elapsed timeout mode and screen-off mode follow the selected behavior. Test rapid switching, recent apps and split screen; record any missed prompts and visible-content limitations.
- Create two profiles with overlapping fixture memberships; activate both, edit membership and deactivate each. Verify individually locked apps remain protected. Confirm that PIN authentication is required before saving, activation, deactivation or deletion.
- Restart the service, terminate and recreate the app process, restart the device, revoke and restore accessibility through system settings. Confirm sessions expire and degraded status is visible. Verify owner settings, emergency access and uninstall remain available.
- Enroll a strong biometric on supported hardware; authenticate, change enrollment and verify the documented PIN recovery path. Do not assume an emulator validates real biometric hardware.
- Upgrade a representative legacy installation using the existing signing certificate; confirm all user settings, credentials, events and protected apps survive. Test storage failure without destructive recovery.

## Accessibility and performance

- Review English, Sinhala and Tamil with native-language reviewers. Test large text, landscape, narrow screens, TalkBack, keyboard navigation and system reduced-motion settings.
- Measure cold startup and lock-screen latency using device traces; record median and tail latency, memory, CPU and battery over a defined test interval. No measurement claim is made until recorded.

Android window-event-based app locking is a best-effort app-layer control, not an operating-system security boundary. See [Android activity security](https://developer.android.com/guide/components/activities/secure-bal) and [OWASP MASVS](https://mas.owasp.org/MASVS/) when recording platform, authentication, storage, cryptographic and privacy evidence.
