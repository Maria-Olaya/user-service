package eafit.gruopChat.grpc;

import org.springframework.beans.factory.annotation.Autowired;

import eafit.gruopChat.user.model.User;
import eafit.gruopChat.user.repository.UserRepository;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
public class UserGrpcServiceImpl extends UserGrpcServiceGrpc.UserGrpcServiceImplBase {

    @Autowired
    private UserRepository userRepository;

    @Override
    public void getUserById(UserIdRequest req, StreamObserver<UserResponse> res) {
        try {
            Long id = Long.parseLong(req.getUserId());
            userRepository.findById(id)
                .ifPresentOrElse(
                    user -> { res.onNext(toProto(user)); res.onCompleted(); },
                    () -> res.onError(Status.NOT_FOUND
                        .withDescription("User not found").asRuntimeException())
                );
        } catch (NumberFormatException e) {
            res.onError(Status.INVALID_ARGUMENT
                .withDescription("Invalid user id").asRuntimeException());
        }
    }

    @Override
    public void existsUser(UserIdRequest req, StreamObserver<ExistsResponse> res) {
        try {
            Long id = Long.parseLong(req.getUserId());
            boolean exists = userRepository.existsById(id);
            res.onNext(ExistsResponse.newBuilder().setExists(exists).build());
            res.onCompleted();
        } catch (NumberFormatException e) {
            res.onNext(ExistsResponse.newBuilder().setExists(false).build());
            res.onCompleted();
        }
    }

    @Override
    public void getUserByEmail(EmailRequest req, StreamObserver<UserResponse> res) {
        userRepository.findByEmail(req.getEmail())
            .ifPresentOrElse(
                user -> { res.onNext(toProto(user)); res.onCompleted(); },
                () -> res.onError(Status.NOT_FOUND
                    .withDescription("User not found").asRuntimeException())
            );
    }

    private UserResponse toProto(User user) {
        return UserResponse.newBuilder()
            .setId(String.valueOf(user.getUserId()))
            .setUsername(user.getName())
            .setEmail(user.getEmail())
            .setEnabled(user.isEnabled())
            .build();
    }
}