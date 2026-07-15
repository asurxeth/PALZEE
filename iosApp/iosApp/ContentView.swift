import SwiftUI
import shared
import AuthenticationServices

struct ContentView: View {
    @State private var text: String = "Welcome to Palzee iOS"
    @State private var rawNonce: String = "mock_raw_nonce_value"

    var body: some View {
        VStack(spacing: 25) {
            Image(systemName: "globe")
                .imageScale(.large)
                .foregroundColor(.accentColor)
            
            Text(text)
                .font(.title2)
                .fontWeight(.bold)
                .multilineTextAlignment(.center)
                .padding(.horizontal)

            SignInWithAppleButton(
                .continue,
                onRequest: { request in
                    request.requestedScopes = [.email, .fullName]
                    request.nonce = "mock_hashed_nonce"
                },
                onCompletion: { result in
                    switch result {
                    case .success(let authorization):
                        if let appleIDCredential = authorization.credential as? ASAuthorizationAppleIDCredential,
                           let identityTokenData = appleIDCredential.identityToken,
                           let idTokenString = String(data: identityTokenData, encoding: .utf8) {
                            
                            Task {
                                do {
                                    // Initialize KMP SDK
                                    IOSPlatformSDK.shared.initialize(
                                        url: "https://your-project.supabase.co",
                                        anonKey: "your-anon-key"
                                    )
                                    
                                    let routeState = try await IOSPlatformSDK.shared.authRepository.authenticateAndRouteUserIdToken(
                                        idToken: idTokenString,
                                        provider: "apple",
                                        nonce: rawNonce
                                    )
                                    
                                    await MainActor.run {
                                        text = "Apple sign-in complete! Route: \(routeState)"
                                    }
                                } catch {
                                    await MainActor.run {
                                        text = "Auth failed: \(error.localizedDescription)"
                                    }
                                }
                            }
                        }
                    case .failure(let error):
                        text = "Sign in failed: \(error.localizedDescription)"
                    }
                }
            )
            .signInWithAppleButtonStyle(.black)
            .frame(height: 50)
            .cornerRadius(12)
            .padding(.horizontal, 40)
        }
        .padding()
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
