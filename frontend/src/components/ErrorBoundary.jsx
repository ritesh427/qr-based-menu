import React from "react";

export default class ErrorBoundary extends React.Component {
  constructor(props) {
    super(props);
    this.state = { hasError: false, message: "" };
  }

  static getDerivedStateFromError(error) {
    return {
      hasError: true,
      message: error?.message || "Unknown frontend error"
    };
  }

  componentDidCatch(error) {
    console.error("Frontend crashed:", error);
  }

  render() {
    if (this.state.hasError) {
      return (
        <main className="mx-auto max-w-3xl px-4 py-10">
          <div className="glass-card space-y-4 p-6">
            <p className="text-xs uppercase tracking-[0.3em] text-red-600">Frontend Error</p>
            <h1 className="font-display text-3xl text-sand-900">The page crashed while rendering</h1>
            <p className="text-sm text-sand-700">
              This confirms the frontend is loading, but React hit a runtime error.
            </p>
            <pre className="overflow-auto rounded-2xl bg-sand-100 p-4 text-sm text-sand-900">
              {this.state.message}
            </pre>
          </div>
        </main>
      );
    }

    return this.props.children;
  }
}
