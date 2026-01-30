IF (Pressione > 100 AND Livello < 50) THEN
    Valvola := (Portata * 1.5) + Offset;
ELSE
    Valvola := 0.0;
END_IF;