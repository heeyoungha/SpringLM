package com.example.springlm.config;

import com.example.springlm.board.Board;
import com.example.springlm.board.BoardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class BoardSeeder implements ApplicationRunner {
    
    @Autowired
    private BoardRepository boardRepository;
    
    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (boardRepository.count() == 0) {
            createBoards();
            System.out.println("게시글 10개가 생성되었습니다.");
        }
    }
    
    private void createBoards() {
        List<Board> boards = Arrays.asList(
            new Board("스프링 부트 시작하기", "스프링 부트로 웹 애플리케이션을 만드는 방법에 대해 알아봅시다. 스프링 부트는 스프링 프레임워크를 기반으로 한 빠른 개발을 위한 도구입니다.", "admin", "스프링"),
            new Board("JPA 기본 개념", "JPA와 Hibernate의 기본 개념과 사용법을 정리했습니다. 객체지향 프로그래밍과 관계형 데이터베이스 간의 불일치를 해결해주는 기술입니다.", "user1", "JPA"),
            new Board("REST API 설계 원칙", "좋은 REST API를 설계하기 위한 원칙들을 소개합니다. RESTful한 API 설계는 개발자 경험과 유지보수성을 크게 향상시킵니다.", "admin", "API"),
            new Board("MySQL 성능 최적화", "데이터베이스 쿼리 성능을 향상시키는 방법들입니다. 인덱스 활용, 쿼리 최적화, 정규화 등의 기법을 다룹니다.", "user2", "데이터베이스"),
            new Board("Git 브랜치 전략", "효과적인 Git 브랜치 관리 전략에 대해 알아봅시다. Git Flow, GitHub Flow 등 다양한 브랜치 전략을 비교해봅니다.", "user1", "Git"),
            new Board("Docker 컨테이너 활용", "Docker를 이용한 애플리케이션 배포 방법입니다. 컨테이너화를 통해 개발 환경과 운영 환경의 일관성을 보장할 수 있습니다.", "admin", "Docker"),
            new Board("Java 8 스트림 API", "Java 8에서 추가된 스트림 API 사용법을 정리했습니다. 함수형 프로그래밍 스타일로 컬렉션을 다루는 방법을 배워봅시다.", "user3", "Java"),
            new Board("테스트 코드 작성하기", "JUnit을 이용한 단위 테스트 작성 방법입니다. 테스트 주도 개발(TDD)의 중요성과 효과적인 테스트 작성 기법을 다룹니다.", "user2", "테스트"),
            new Board("스프링 시큐리티 설정", "웹 애플리케이션의 보안을 위한 스프링 시큐리티 설정법입니다. 인증과 인가, CSRF 보호 등의 보안 기능을 구현해봅시다.", "admin", "보안"),
            new Board("프론트엔드와 백엔드 연동", "React와 Spring Boot를 연동하는 방법을 알아봅시다. REST API를 통한 데이터 통신과 CORS 설정 등을 다룹니다.", "user1", "연동")
        );
        
        boardRepository.saveAll(boards);
    }
}