PressioneBar := (SegnalePressione * 0.01) + Offset;

IF (PressioneBar > SogliaMassima) OR (Emergenza = TRUE) THEN
    ValvolaScarico := TRUE;
    PompaMarcia := FALSE;
ELSE
    ValvolaScarico := FALSE;
    PompaMarcia := TRUE;
END_IF;