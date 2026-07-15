import SwiftUI
import shared
import AuthenticationServices

struct ContentView: View {
    @State private var screenState: ScreenState = .main
    @State private var emailInput: String = ""
    @State private var backupEmailInput: String = ""
    @FocusState private var isEmailFocused: Bool
    
    enum ScreenState {
        case main
        case troubleOverlay
        case emailSheet
        case backupEmailSheet
    }
    
    // Haptics Helper
    private func triggerHaptic() {
        let generator = UIImpactFeedbackGenerator(style: .medium)
        generator.impactOccurred()
    }
    
    var body: some View {
        ZStack {
            // Background (Matches light mode exactly)
            Color(red: 242/255, green: 242/255, blue: 242/255)
                .ignoresSafeArea()
            
            // Main Onboarding Layout
            VStack(spacing: 0) {
                // Top Doodle Area
                ZStack {
                    // Cloud character in the center
                    Image("pal_circular_logo")
                        .resizable()
                        .scaledToFit()
                        .frame(width: 140, height: 140)
                    
                    // Envelope left of cloud
                    Image("dm_envalope")
                        .resizable()
                        .scaledToFit()
                        .frame(width: 50, height: 50)
                        .offset(x: -110, y: 30)
                    
                    // Moon right of cloud
                    Image("dm_moon")
                        .resizable()
                        .scaledToFit()
                        .frame(width: 55, height: 55)
                        .offset(x: 110, y: 35)
                    
                    // Star doodles around top
                    Image("dm_star_1")
                        .resizable()
                        .scaledToFit()
                        .frame(width: 25, height: 25)
                        .offset(x: -115, y: -45)
                    
                    Image("dm_star_2")
                        .resizable()
                        .scaledToFit()
                        .frame(width: 45, height: 45)
                        .offset(x: -70, y: -50)
                    
                    Image("dm_star_3")
                        .resizable()
                        .scaledToFit()
                        .frame(width: 25, height: 25)
                        .offset(x: 15, y: -45)
                    
                    Image("dm_star_4")
                        .resizable()
                        .scaledToFit()
                        .frame(width: 45, height: 45)
                        .offset(x: 80, y: -50)
                    
                    Image("dm_star_5")
                        .resizable()
                        .scaledToFit()
                        .frame(width: 25, height: 25)
                        .offset(x: 135, y: 25)
                }
                .frame(height: 180)
                .padding(.top, 10)
                
                // Title Area
                VStack(spacing: 8) {
                    Text("Pal")
                        .font(.system(size: 34, weight: .black, design: .rounded))
                        .foregroundColor(.black)
                    
                    Text("new moment every hour,\nvlog it with your friends.")
                        .font(.system(size: 17, weight: .semibold, design: .default))
                        .foregroundColor(.black.opacity(0.8))
                        .multilineTextAlignment(.center)
                        .lineSpacing(4)
                }
                .padding(.vertical, 10)
                
                // Middle Doodle Area
                ZStack {
                    // Pizza slice left
                    Image("dm_pizza")
                        .resizable()
                        .scaledToFit()
                        .frame(width: 85, height: 85)
                        .offset(x: -115, y: -10)
                    
                    // Orange center
                    Image("dm_orange")
                        .resizable()
                        .scaledToFit()
                        .frame(width: 35, height: 35)
                        .offset(x: -20, y: 15)
                    
                    // Plant in purple vase right
                    Image("dm_plant")
                        .resizable()
                        .scaledToFit()
                        .frame(width: 140, height: 110)
                        .offset(x: 105, y: 15)
                }
                .frame(height: 110)
                
                Spacer()
                
                // Onboarding Action Buttons
                VStack(spacing: 12) {
                    // Button 1: Connect with Passkey
                    Button(action: {
                        triggerHaptic()
                        // Native passkey action placeholder
                    }) {
                        HStack(spacing: 8) {
                            Image(systemName: "person.badge.key.fill")
                                .font(.system(size: 18))
                            Text("Connect with Passkey")
                                .font(.system(size: 17, weight: .bold))
                        }
                        .foregroundColor(.white)
                        .frame(maxWidth: .infinity)
                        .frame(height: 52)
                        .background(Color.black)
                        .cornerRadius(26)
                    }
                    
                    // Button 2: Connect with Apple
                    Button(action: {
                        triggerHaptic()
                        // Native Apple Sign-In Integration
                    }) {
                        HStack(spacing: 8) {
                            Image(systemName: "apple.logo")
                                .font(.system(size: 19))
                            Text("Connect with Apple")
                                .font(.system(size: 17, weight: .bold))
                        }
                        .foregroundColor(.white)
                        .frame(maxWidth: .infinity)
                        .frame(height: 52)
                        .background(Color.black)
                        .cornerRadius(26)
                    }
                    
                    // Troubleshooting flow link
                    Button(action: {
                        triggerHaptic()
                        withAnimation(.spring(response: 0.35, dampingFraction: 0.8)) {
                            screenState = .troubleOverlay
                        }
                    }) {
                        Text("having trouble logging in?")
                            .font(.system(size: 15, weight: .semibold))
                            .foregroundColor(.black)
                            .underline()
                            .padding(.top, 10)
                    }
                }
                .padding(.horizontal, 32)
                
                Spacer()
                
                // Bottom Doodle Area
                ZStack {
                    // Tea cup left
                    Image("dm_tea")
                        .resizable()
                        .scaledToFit()
                        .frame(width: 75, height: 75)
                        .offset(x: -110, y: 0)
                    
                    // Fire center
                    Image("dm_fire")
                        .resizable()
                        .scaledToFit()
                        .frame(width: 60, height: 50)
                        .offset(x: 5, y: 15)
                    
                    // Ice cream cup right
                    Image("dm_bingsu")
                        .resizable()
                        .scaledToFit()
                        .frame(width: 65, height: 75)
                        .offset(x: 110, y: 0)
                }
                .frame(height: 90)
                .padding(.bottom, 20)
            }
            .blur(radius: (screenState == .emailSheet || screenState == .backupEmailSheet) ? 4 : 0)
            
            // Dimmed overlay background
            if screenState != .main {
                Color.black.opacity(screenState == .troubleOverlay ? 0.15 : 0.3)
                    .ignoresSafeArea()
                    .onTapGesture {
                        withAnimation(.spring(response: 0.35, dampingFraction: 0.8)) {
                            screenState = .main
                            isEmailFocused = false
                        }
                    }
            }
            
            // Frosted Glass Trouble Selector (Image 2 Overlay)
            if screenState == .troubleOverlay {
                VStack {
                    Spacer()
                    VStack(spacing: 0) {
                        Capsule()
                            .frame(width: 40, height: 5)
                            .foregroundColor(.gray.opacity(0.5))
                            .padding(.top, 8)
                        
                        Text("having trouble logging in?")
                            .font(.system(size: 15, weight: .bold))
                            .foregroundColor(.gray)
                            .padding(.top, 15)
                            .padding(.bottom, 20)
                        
                        // Option 1: Restore from backup email
                        Button(action: {
                            triggerHaptic()
                            withAnimation(.spring(response: 0.35, dampingFraction: 0.8)) {
                                screenState = .backupEmailSheet
                            }
                        }) {
                            HStack(spacing: 12) {
                                Image(systemName: "icloud.and.arrow.down.fill")
                                    .font(.system(size: 20))
                                    .foregroundColor(.black)
                                VStack(alignment: .leading, spacing: 2) {
                                    Text("Restore from")
                                        .font(.system(size: 16, weight: .bold))
                                    Text("backup email")
                                        .font(.system(size: 16, weight: .bold))
                                }
                                Spacer()
                            }
                            .foregroundColor(.black)
                            .padding(.horizontal, 24)
                            .frame(height: 60)
                        }
                        
                        Divider()
                            .padding(.horizontal, 24)
                        
                        // Option 2: Continue with email
                        Button(action: {
                            triggerHaptic()
                            withAnimation(.spring(response: 0.35, dampingFraction: 0.8)) {
                                screenState = .emailSheet
                            }
                        }) {
                            HStack(spacing: 12) {
                                Image(systemName: "envelope.fill")
                                    .font(.system(size: 18))
                                    .foregroundColor(.black)
                                Text("Continue with email")
                                    .font(.system(size: 16, weight: .bold))
                                Spacer()
                            }
                            .foregroundColor(.black)
                            .padding(.horizontal, 24)
                            .frame(height: 60)
                        }
                        .padding(.bottom, 20)
                    }
                    .background(
                        RoundedRectangle(cornerRadius: 30)
                            .fill(.ultraThinMaterial)
                    )
                    .clipShape(RoundedRectangle(cornerRadius: 30))
                    .shadow(color: Color.black.opacity(0.15), radius: 20, x: 0, y: 10)
                    .padding(.horizontal, 20)
                    .padding(.bottom, 40)
                    .transition(.move(edge: .bottom).combined(with: .opacity))
                }
            }
            
            // Email Sign In Dialog (Image 3 & 4)
            if screenState == .emailSheet {
                emailSheetView(
                    title: "Continue with email",
                    description: "Enter your email to get a code.",
                    placeholder: "Email address",
                    input: $emailInput,
                    onClose: {
                        withAnimation(.spring(response: 0.35, dampingFraction: 0.8)) {
                            screenState = .main
                            isEmailFocused = false
                        }
                    },
                    onSubmit: {
                        triggerHaptic()
                        // Execute KMP Supabase send OTP
                        Task {
                            do {
                                IOSPlatformSDK.shared.initialize(
                                    url: "https://your-project.supabase.co",
                                    anonKey: "your-anon-key"
                                )
                                try await IOSPlatformSDK.shared.authRepository.sendEmailOtp(email: emailInput)
                            } catch {
                                print("OTP Send Failed: \(error.localizedDescription)")
                            }
                        }
                    }
                )
                .transition(.move(edge: .bottom))
            }
            
            // Backup Email Dialog (Image 5)
            if screenState == .backupEmailSheet {
                emailSheetView(
                    title: "Sign in with a\nbackup email",
                    description: "Enter your backup email to get a sign-in code.",
                    placeholder: "Email address",
                    input: $backupEmailInput,
                    onClose: {
                        withAnimation(.spring(response: 0.35, dampingFraction: 0.8)) {
                            screenState = .main
                            isEmailFocused = false
                        }
                    },
                    onSubmit: {
                        triggerHaptic()
                        // Execute KMP verify logic
                    }
                )
                .transition(.move(edge: .bottom))
            }
        }
        .preferredColorScheme(.light) // Force light theme styling
    }
    
    // Custom reusable sheet for email inputs
    @ViewBuilder
    private func emailSheetView(
        title: String,
        description: String,
        placeholder: String,
        input: Binding<String>,
        onClose: @escaping () -> Void,
        onSubmit: @escaping () -> Void
    ) -> some View {
        VStack {
            Spacer()
            
            VStack(spacing: 0) {
                // Header (Grabber + Title + Close Button)
                ZStack {
                    Capsule()
                        .frame(width: 40, height: 5)
                        .foregroundColor(.gray.opacity(0.3))
                        .offset(y: -25)
                    
                    Text("Email")
                        .font(.system(size: 16, weight: .bold))
                        .foregroundColor(.black)
                    
                    HStack {
                        Spacer()
                        Button(action: {
                            triggerHaptic()
                            onClose()
                        }) {
                            Image(systemName: "xmark")
                                .font(.system(size: 16, weight: .bold))
                                .foregroundColor(.black)
                                .frame(width: 32, height: 32)
                                .background(Color(red: 240/255, green: 240/255, blue: 240/255))
                                .clipShape(Circle())
                        }
                    }
                }
                .padding(.horizontal, 24)
                .padding(.top, 35)
                
                // Form Fields
                VStack(alignment: .leading, spacing: 10) {
                    Text(title)
                        .font(.system(size: 28, weight: .bold))
                        .foregroundColor(.black)
                        .lineSpacing(2)
                    
                    Text(description)
                        .font(.system(size: 15, weight: .medium))
                        .foregroundColor(.gray)
                        .padding(.bottom, 15)
                    
                    // Input TextField
                    ZStack(alignment: .leading) {
                        if input.wrappedValue.isEmpty {
                            Text(placeholder)
                                .foregroundColor(.gray.opacity(0.5))
                                .padding(.horizontal, 20)
                        }
                        TextField("", text: input)
                            .foregroundColor(.black)
                            .keyboardType(.emailAddress)
                            .autocapitalization(.none)
                            .disableAutocorrection(true)
                            .focused($isEmailFocused)
                            .padding(.horizontal, 20)
                    }
                    .frame(height: 54)
                    .background(Color(red: 242/255, green: 242/255, blue: 242/255))
                    .cornerRadius(12)
                    .padding(.bottom, 15)
                    
                    // Send Code Button (Turns black when first character is entered)
                    Button(action: {
                        if !input.wrappedValue.isEmpty {
                            onSubmit()
                        }
                    }) {
                        HStack(spacing: 8) {
                            Image(systemName: "paperplane.fill")
                                .font(.system(size: 15))
                            Text("Send code")
                                .font(.system(size: 17, weight: .bold))
                        }
                        .foregroundColor(input.wrappedValue.isEmpty ? .gray : .white)
                        .frame(maxWidth: .infinity)
                        .frame(height: 52)
                        .background(input.wrappedValue.isEmpty ? Color(red: 224/255, green: 224/255, blue: 224/255) : Color.black)
                        .cornerRadius(26)
                        .animation(.easeInOut(duration: 0.25), value: input.wrappedValue.isEmpty)
                    }
                }
                .padding(.horizontal, 24)
                .padding(.bottom, 40)
            }
            .background(Color.white)
            .clipShape(RoundedRectangle(cornerRadius: 36))
            .shadow(color: Color.black.opacity(0.15), radius: 30, x: 0, y: -10)
        }
        .ignoresSafeArea(.keyboard)
        .onAppear {
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.15) {
                isEmailFocused = true
            }
        }
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
