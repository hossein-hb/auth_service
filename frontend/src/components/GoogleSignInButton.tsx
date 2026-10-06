import { useEffect, useRef, useState } from 'react';

interface GoogleSignInButtonProps {
  onSuccess: (idToken: string) => void;
  onError?: () => void;
  text?: 'signin_with' | 'signup_with' | 'continue_with' | 'signin';
}

const GOOGLE_CLIENT_ID = import.meta.env.VITE_GOOGLE_CLIENT_ID;

export default function GoogleSignInButton({
  onSuccess,
  onError,
  text = 'continue_with',
}: GoogleSignInButtonProps) {

  const buttonRef = useRef<HTMLDivElement | null>(null);
  const [scriptLoaded, setScriptLoaded] = useState(
    () => Boolean(window.google)
  );

  useEffect(() => {

    if (window.google) {
      setScriptLoaded(true);
      return;
    }

    const existingScript =
      document.querySelector(
        'script[src="https://accounts.google.com/gsi/client"]'
      );

    if (existingScript) {

      const handleLoad = () => {
        setScriptLoaded(true);
      };

      existingScript.addEventListener('load', handleLoad);

      return () => {
        existingScript.removeEventListener('load', handleLoad);
      };
    }

    const script = document.createElement('script');

    script.src =
      'https://accounts.google.com/gsi/client';

    script.async = true;
    script.defer = true;

    script.onload = () => {
      setScriptLoaded(true);
    };

    script.onerror = () => {
      onError?.();
    };

    document.head.appendChild(script);

  }, [onError]);

  useEffect(() => {

    if (!scriptLoaded) {
      return;
    }

    if (!window.google) {
      onError?.();
      return;
    }

    if (!GOOGLE_CLIENT_ID) {
      console.error(
        'VITE_GOOGLE_CLIENT_ID is not configured.'
      );
      onError?.();
      return;
    }

    if (!buttonRef.current) {
      return;
    }

    buttonRef.current.innerHTML = '';

    window.google.accounts.id.initialize({
      client_id: GOOGLE_CLIENT_ID,

      callback: (response) => {

        if (!response.credential) {
          onError?.();
          return;
        }

        onSuccess(response.credential);
      },

      auto_select: false,
      cancel_on_tap_outside: true,
    });

    window.google.accounts.id.renderButton(
      buttonRef.current,
      {
        theme: 'outline',
        size: 'large',
        text,
        shape: 'rectangular',
        width: 320,
      }
    );

  }, [
    scriptLoaded,
    onSuccess,
    onError,
    text,
  ]);

  return (
    <div
      ref={buttonRef}
      style={{
        display: 'flex',
        justifyContent: 'center',
        minHeight: '44px',
      }}
    />
  );
}