package me.jioniy.jwttutorial.repository;

import me.jioniy.jwttutorial.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * User 엔티티에 매핑되는 repository 인터페이스
 * JpaRepository를 상속받아 findAll, save 등의 메소드를 기본적으로 사용
 */
public interface UserRepository extends JpaRepository<User,Long> {

    /**
     * Username를 기준으로 user 객체를 가져올 때 권한 정보도 함께 가져올 수 있도록 설정
     * @EntityGraph - 쿼리가 수행될 때 Lazy 조회가 아닌 Eager 조회로, authorities 정보를 같이 가져옴
     * */
    @EntityGraph(attributePaths = "authorities")
    Optional<User> findOneWithAuthoritiesByUsername(String username);

}
