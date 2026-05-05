// Fix for sockjs-client + webpack 5 incompatibility in test environment
// ReferenceError: global is not defined
(window as any).global = window;
